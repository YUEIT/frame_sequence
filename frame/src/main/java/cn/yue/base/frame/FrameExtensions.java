package cn.yue.base.frame;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.annotation.GlideExtension;
import com.bumptech.glide.annotation.GlideOption;
import com.bumptech.glide.annotation.GlideType;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.github.penfeizhou.animation.apng.APNGDrawable;

import cn.yue.base.frame.anim.AnimFrameDrawable;
import cn.yue.base.frame.apng.loader.ApngFrame;
import cn.yue.base.frame.gif.GifFrameDrawable;

/**
 * Created by Administrator on 2018\11\19 0019.
 */

@GlideExtension
public class FrameExtensions {
    private FrameExtensions() {
    }

    final static RequestOptions DECODE_TYPE_FRAME = RequestOptions
            .decodeTypeOf(GifFrameDrawable.class)
            .lock();

    @GlideType(GifFrameDrawable.class)
    public static RequestBuilder<GifFrameDrawable> asFrame(RequestBuilder<GifFrameDrawable> requestBuilder) {
        return requestBuilder.apply(DECODE_TYPE_FRAME);
    }

    final static RequestOptions DECODE_TYPE_SINGLE_FRAME = RequestOptions
            .decodeTypeOf(ApngFrame.class)
            .lock();

    @GlideType(ApngFrame.class)
    public static RequestBuilder<ApngFrame> singleFrame(RequestBuilder<ApngFrame> requestBuilder) {
        return requestBuilder.apply(DECODE_TYPE_SINGLE_FRAME);
    }

    final static RequestOptions DECODE_TYPE_ANIM_FRAME = RequestOptions.decodeTypeOf(AnimFrameDrawable.class).lock();

    @GlideType(AnimFrameDrawable.class)
    public static RequestBuilder<AnimFrameDrawable> asAnim(RequestBuilder<AnimFrameDrawable> requestBuilder) {
        return requestBuilder.apply(DECODE_TYPE_ANIM_FRAME);
    }

    @GlideOption
    public static BaseRequestOptions<?> cache(BaseRequestOptions<?> options) {
        return options.set(FrameOption.optionCache(), true);
    }

    @GlideOption
    public static BaseRequestOptions<?> duration(BaseRequestOptions<?> options, int duration) {
        return options.set(FrameOption.optionDuration(), duration);
    }

    final static RequestOptions DECODE_TYPE_APNG_FRAME = RequestOptions.decodeTypeOf(APNGDrawable.class).lock();

    @GlideType(APNGDrawable.class)
    public static RequestBuilder<APNGDrawable> asApng(RequestBuilder<APNGDrawable> requestBuilder) {
        return requestBuilder.apply(DECODE_TYPE_APNG_FRAME);
    }
}
