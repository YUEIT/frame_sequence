package cn.yue.base.frame.apng.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.UnitTransformation;
import com.bumptech.glide.load.resource.gif.GifOptions;

import java.io.IOException;


/**
 * Description :
 * Created by yue on 2021/12/8
 */

public class ApngWrapperToDrawableDecoder implements ResourceDecoder<ApngWrapper, ApngDrawable> {

    private static final FrameDecoderFactory FRAME_DECODER_FACTORY = new FrameDecoderFactory();
    private final Context context;

    public ApngWrapperToDrawableDecoder(Context context) {
        this.context = context;
    }

    @Override
    public boolean handles(@NonNull ApngWrapper source, @NonNull Options options) throws IOException {
        return true;
    }

    @Nullable
    @Override
    public Resource<ApngDrawable> decode(@NonNull ApngWrapper source, int width, int height, @NonNull Options options) throws IOException {
        try {
            Bitmap.Config config =
                    options.get(GifOptions.DECODE_FORMAT) == DecodeFormat.PREFER_RGB_565
                            ? Bitmap.Config.RGB_565
                            : Bitmap.Config.ARGB_8888;
            ApngDecoder frameDecoder = FRAME_DECODER_FACTORY.build(context, source, config);
            if (frameDecoder == null) {
                return null;
            }
            frameDecoder.advance();
            ApngFrame firstFrame = frameDecoder.getNextFrame();
            if (firstFrame == null) {
                return null;
            }
            Transformation<ApngFrame> unitTransformation = UnitTransformation.get();
            ApngDrawable frameDrawable =
                    new ApngDrawable(context, frameDecoder, unitTransformation, frameDecoder.getWidth(), frameDecoder.getHeight(), firstFrame);

            return new ApngDrawableResource(frameDrawable);
        } catch (Exception e) {
            return null;
        }
    }

    @VisibleForTesting
    static class FrameDecoderFactory {
        ApngDecoder build(
                Context context, ApngWrapper data, Bitmap.Config config) {
            try {
                return new ApngDecoder(context, data, config);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
