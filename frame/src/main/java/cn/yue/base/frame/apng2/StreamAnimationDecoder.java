package cn.yue.base.frame.apng2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.github.penfeizhou.animation.apng.decode.APNGDecoder;
import com.github.penfeizhou.animation.apng.decode.APNGParser;
import com.github.penfeizhou.animation.decode.FrameSeqDecoder;
import com.github.penfeizhou.animation.io.StreamReader;
import com.github.penfeizhou.animation.loader.Loader;
import com.github.penfeizhou.animation.loader.StreamLoader;

import java.io.IOException;
import java.io.InputStream;

public class StreamAnimationDecoder implements ResourceDecoder<InputStream, FrameSeqDecoder> {

    @Override
    public boolean handles(@NonNull InputStream source, @NonNull Options options) {
        try {
            return APNGParser.isAPNG(new StreamReader(source));
        } catch (Exception e) {
            return false;
        }
    }

    @Nullable
    @Override
    public Resource<FrameSeqDecoder> decode(@NonNull final InputStream source, int width, int height, @NonNull Options options) throws IOException {
        Loader loader = new StreamLoader() {
            @Override
            protected InputStream getInputStream() throws IOException {
                return source;
            }
        };
        final FrameSeqDecoder decoder = new APNGDecoder(loader, null);
        return new FrameSeqDecoderResource(decoder, 0);
    }

}
