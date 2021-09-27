package cn.yue.base.frame.glide;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.ImageHeaderParser;
import com.bumptech.glide.load.ImageHeaderParserUtils;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.resource.gif.GifOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class StreamFrameDrawableDecoder implements ResourceDecoder<InputStream, FrameDrawable> {

    private static final String TAG = "StreamFrameDecoder";

    private final List<ImageHeaderParser> parsers;
    private final ResourceDecoder<ByteBuffer, FrameDrawable> byteBufferDecoder;
    private final ArrayPool byteArrayPool;

    public StreamFrameDrawableDecoder(Context context, ResourceDecoder<ByteBuffer, FrameDrawable> byteBufferDecoder) {
        this(
                context,
                Glide.get(context).getRegistry().getImageHeaderParsers(),
                Glide.get(context).getArrayPool(),
                byteBufferDecoder);
    }

    public StreamFrameDrawableDecoder(
            Context context,
            List<ImageHeaderParser> parsers,
            ArrayPool byteArrayPool,
            ResourceDecoder<ByteBuffer, FrameDrawable> byteBufferDecoder) {
        this.parsers = parsers;
        this.byteBufferDecoder = byteBufferDecoder;
        this.byteArrayPool = byteArrayPool;
    }

    @Override
    public boolean handles(@NonNull InputStream source, @NonNull Options options) throws IOException {
        return !options.get(GifOptions.DISABLE_ANIMATION)
                && ImageHeaderParserUtils.getType(parsers, source, byteArrayPool) == ImageHeaderParser.ImageType.GIF;
    }

    @Nullable
    @Override
    public Resource<FrameDrawable> decode(@NonNull InputStream source, int width, int height, @NonNull Options options) throws IOException {
        byte[] data = inputStreamToBytes(source);
        if (data == null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        return byteBufferDecoder.decode(byteBuffer, width, height, options);
    }

    private static byte[] inputStreamToBytes(InputStream is) {
        final int bufferSize = 16384;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(bufferSize);
        try {
            int nRead;
            byte[] data = new byte[bufferSize];
            while ((nRead = is.read(data)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException e) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Error reading data from stream", e);
            }
            return null;
        }
        return buffer.toByteArray();
    }
}
