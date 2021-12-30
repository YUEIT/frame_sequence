package cn.yue.base.frame.gif;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.ImageHeaderParser;
import com.bumptech.glide.load.ImageHeaderParserUtils;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.UnitTransformation;
import com.bumptech.glide.load.resource.gif.GifOptions;
import com.bumptech.glide.util.LogTime;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class ByteFrameDrawableDecoder implements ResourceDecoder<ByteBuffer, GifFrameDrawable> {
    
    private static final String TAG = "BufferGifDecoder";
    private static final FrameDecoderFactory FRAME_DECODER_FACTORY = new FrameDecoderFactory();

    private final Context context;
    private final List<ImageHeaderParser> parsers;
    private final FrameDecoderFactory frameDecoderFactory;
    private final FrameBitmapProvider provider;

    // Public API.
    public ByteFrameDrawableDecoder(Context context) {
        this(
                context,
                Glide.get(context).getRegistry().getImageHeaderParsers(),
                Glide.get(context).getBitmapPool(),
                Glide.get(context).getArrayPool());
    }

    public ByteFrameDrawableDecoder(
            Context context,
            List<ImageHeaderParser> parsers,
            BitmapPool bitmapPool,
            ArrayPool arrayPool) {
        this(context, parsers, bitmapPool, arrayPool, FRAME_DECODER_FACTORY);
    }

    @VisibleForTesting
    ByteFrameDrawableDecoder(
            Context context,
            List<ImageHeaderParser> parsers,
            BitmapPool bitmapPool,
            ArrayPool arrayPool,
            FrameDecoderFactory frameDecoderFactory) {
        this.context = context.getApplicationContext();
        this.parsers = parsers;
        this.frameDecoderFactory = frameDecoderFactory;
        provider = new FrameBitmapProvider(bitmapPool, arrayPool);
    }

    @Override
    public boolean handles(@NonNull ByteBuffer source, @NonNull Options options) throws IOException {
        return !options.get(GifOptions.DISABLE_ANIMATION)
                && ImageHeaderParserUtils.getType(parsers, source) == ImageHeaderParser.ImageType.GIF;
    }

    @Override
    public FrameDrawableResource decode(
            @NonNull ByteBuffer source, int width, int height, @NonNull Options options) {
        long startTime = LogTime.getLogTime();
        try {
            Bitmap.Config config =
                    options.get(GifOptions.DECODE_FORMAT) == DecodeFormat.PREFER_RGB_565
                            ? Bitmap.Config.RGB_565
                            : Bitmap.Config.ARGB_8888;
            FrameDecoder frameDecoder = frameDecoderFactory.build(provider, source, config);
            if (frameDecoder == null) {
                return null;
            }
            frameDecoder.advance();
            Bitmap firstFrame = frameDecoder.getNextFrame();
            if (firstFrame == null) {
                return null;
            }

            Transformation<Bitmap> unitTransformation = UnitTransformation.get();

            GifFrameDrawable gifFrameDrawable =
                    new GifFrameDrawable(context, frameDecoder, unitTransformation, width, height, firstFrame);

            return new FrameDrawableResource(gifFrameDrawable);
        } finally {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Decoded GIF from stream in " + LogTime.getElapsedMillis(startTime));
            }
        }
    }


    @VisibleForTesting
    static class FrameDecoderFactory {
        FrameDecoder build(
                FrameDecoder.BitmapProvider provider, ByteBuffer data, Bitmap.Config config) {
            try {
                return new SequenceFrameDecoder(provider, data, config);
            } catch (Exception e) {
                return null;
            }
        }
    }
}

