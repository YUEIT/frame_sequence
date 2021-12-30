package cn.yue.base.frame.apng2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.drawable.DrawableResource;
import com.github.penfeizhou.animation.apng.APNGDrawable;
import com.github.penfeizhou.animation.apng.decode.APNGDecoder;
import com.github.penfeizhou.animation.decode.FrameSeqDecoder;

import java.io.IOException;


/**
 * Description :
 * Created by yue on 2021/11/29
 */

public class APNGDecoderToFrameDecoder implements ResourceDecoder<FrameSeqDecoder, APNGDrawable> {

    public APNGDecoderToFrameDecoder() {
    }

    @Override
    public boolean handles(@NonNull FrameSeqDecoder source, @NonNull Options options) throws IOException {
        return true;
    }

    @Nullable
    @Override
    public Resource<APNGDrawable> decode(@NonNull FrameSeqDecoder source, int width, int height, @NonNull Options options) throws IOException {
        final APNGDrawable apngDrawable = new APNGDrawable((APNGDecoder) source);
        apngDrawable.setAutoPlay(false);
        apngDrawable.setNoMeasure(true);
        return new DrawableResource<APNGDrawable>(apngDrawable) {
            @NonNull
            @Override
            public Class<APNGDrawable> getResourceClass() {
                return APNGDrawable.class;
            }

            @Override
            public int getSize() {
                return apngDrawable.getMemorySize();
            }

            @Override
            public void recycle() {
                apngDrawable.stop();
            }

            @Override
            public void initialize() {
                super.initialize();
            }
        };
    }


}
