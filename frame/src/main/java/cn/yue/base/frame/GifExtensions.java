package cn.yue.base.frame;

import android.support.rastermill.GlideFrameSequenceDrawable;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.annotation.GlideExtension;
import com.bumptech.glide.annotation.GlideType;
import com.bumptech.glide.request.RequestOptions;

/**
 * Created by Administrator on 2018\11\19 0019.
 */

@GlideExtension
public class GifExtensions {
    private GifExtensions() {
    }

    final static RequestOptions DECODE_TYPE = RequestOptions
            .decodeTypeOf(GlideFrameSequenceDrawable.class)
            .lock();

    @GlideType(GlideFrameSequenceDrawable.class)
    public static RequestBuilder<GlideFrameSequenceDrawable> asFrame(RequestBuilder<GlideFrameSequenceDrawable> requestBuilder) {
        return requestBuilder.apply(DECODE_TYPE);
    }
}
