package cn.yue.base.frame.apng2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.drawable.DrawableResource;
import com.github.penfeizhou.animation.apng.APNGDrawable;
import com.github.penfeizhou.animation.apng.decode.APNGDecoder;
import com.github.penfeizhou.animation.apng.decode.APNGParser;
import com.github.penfeizhou.animation.decode.FrameSeqDecoder;
import com.github.penfeizhou.animation.io.StreamReader;

import java.io.IOException;
import java.io.InputStream;


/**
 * Description :
 * Created by yue on 2021/11/29
 */

public class APNGDecoderToDrawableDecoder implements ResourceDecoder<InputStream, APNGDrawable> {

    ResourceDecoder<InputStream, FrameSeqDecoder> decoder;
    public APNGDecoderToDrawableDecoder(ResourceDecoder<InputStream, FrameSeqDecoder> decoder) {
        this.decoder = decoder;
    }

    @Override
    public boolean handles(@NonNull InputStream source, @NonNull Options options) throws IOException {
        try {
            return APNGParser.isAPNG(new StreamReader(source));
        } catch (Exception e) {
            return false;
        }
    }

    @Nullable
    @Override
    public Resource<APNGDrawable> decode(@NonNull InputStream source, int width, int height, @NonNull Options options) throws IOException {
        Resource<FrameSeqDecoder> frameSeqDecoderResource = decoder.decode(source, width, height, options);
        if (frameSeqDecoderResource == null) {
            return null;
        }
        APNGDecoder frameSeqDecoder = (APNGDecoder)frameSeqDecoderResource.get();
        final APNGDrawable apngDrawable = new APNGDrawable(frameSeqDecoder);
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
