package cn.yue.base.frame.anim;

import android.content.Context;
import android.graphics.Bitmap;

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

import cn.yue.base.frame.FrameOption;


/**
 * Description :
 * Created by yue on 2021/11/29
 */

public class AnimWrapperToDrawableDecoder implements ResourceDecoder<AnimWrapper, AnimFrameDrawable> {

    private static final FrameDecoderFactory FRAME_DECODER_FACTORY = new FrameDecoderFactory();
    private final Context context;

    public AnimWrapperToDrawableDecoder(Context context) {
        this.context = context;
    }

    @Override
    public boolean handles(@NonNull AnimWrapper source, @NonNull Options options) throws IOException {
        return true;
    }

    @Nullable
    @Override
    public Resource<AnimFrameDrawable> decode(@NonNull AnimWrapper source, int width, int height, @NonNull Options options) throws IOException {
        try {
            Bitmap.Config config =
                    options.get(GifOptions.DECODE_FORMAT) == DecodeFormat.PREFER_RGB_565
                            ? Bitmap.Config.RGB_565
                            : Bitmap.Config.ARGB_8888;
            Boolean requireCache = options.get(FrameOption.optionCache());
            Integer duration = options.get(FrameOption.optionDuration());
            if (duration == null || duration == 0) {
                if (!source.hasDuration()) {
                    source.setDuration(50);
                }
            } else {
                source.setDuration(duration);
            }
            IFrameDecoder frameDecoder = FRAME_DECODER_FACTORY.build(context, source,
                    config, requireCache != null ? requireCache : false);
            if (frameDecoder == null) {
                return null;
            }
            frameDecoder.advance();
            Frame firstFrame = frameDecoder.getNextFrame();
            if (firstFrame == null) {
                return null;
            }
            Transformation<Frame> unitTransformation = UnitTransformation.get();
            AnimFrameDrawable frameDrawable =
                    new AnimFrameDrawable(context, frameDecoder, unitTransformation, width, height, firstFrame);
            return new AnimFrameDrawableResource(frameDrawable);
        } catch (Exception e) {
            return null;
        }
    }

    @VisibleForTesting
    static class FrameDecoderFactory {
        IFrameDecoder build(
                Context context, AnimWrapper data, Bitmap.Config config, boolean requireCache) {
            try {
                return new AnimFrameDecoder(context, data, config, requireCache);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
