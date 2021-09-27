package cn.yue.base.frame;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.annotation.GlideExtension;
import com.bumptech.glide.annotation.GlideType;
import com.bumptech.glide.request.RequestOptions;

import cn.yue.base.frame.glide.FrameDrawable;

/**
 * Created by Administrator on 2018\11\19 0019.
 */

@GlideExtension
public class GifExtensions {
    private GifExtensions() {
    }

//    final static RequestOptions DECODE_TYPE = RequestOptions
//            .decodeTypeOf(GlideFrameSequenceDrawable.class)
//            .lock();
//
//    @GlideType(GlideFrameSequenceDrawable.class)
//    public static RequestBuilder<GlideFrameSequenceDrawable> asFrame(RequestBuilder<GlideFrameSequenceDrawable> requestBuilder) {
//        return requestBuilder.apply(DECODE_TYPE);
//    }

    final static RequestOptions DECODE_TYPE_FRAME = RequestOptions
            .decodeTypeOf(FrameDrawable.class)
            .lock();

    @GlideType(FrameDrawable.class)
    public static RequestBuilder<FrameDrawable> asFrame(RequestBuilder<FrameDrawable> requestBuilder) {
        return requestBuilder.apply(DECODE_TYPE_FRAME);
    }
}
