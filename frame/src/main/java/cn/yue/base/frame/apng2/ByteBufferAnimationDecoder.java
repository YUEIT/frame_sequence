package cn.yue.base.frame.apng2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.github.penfeizhou.animation.apng.decode.APNGDecoder;
import com.github.penfeizhou.animation.apng.decode.APNGParser;
import com.github.penfeizhou.animation.decode.FrameSeqDecoder;
import com.github.penfeizhou.animation.io.ByteBufferReader;
import com.github.penfeizhou.animation.loader.ByteBufferLoader;
import com.github.penfeizhou.animation.loader.Loader;

import java.io.IOException;
import java.nio.ByteBuffer;


public class ByteBufferAnimationDecoder implements ResourceDecoder<ByteBuffer, FrameSeqDecoder> {

    @Override
    public boolean handles(@NonNull ByteBuffer source, @NonNull Options options) {
        try {
            return APNGParser.isAPNG(new ByteBufferReader(source));
        } catch (Exception e) {
            return false;
        }
    }

    @Nullable
    @Override
    public Resource<FrameSeqDecoder> decode(@NonNull final ByteBuffer source, int width, int height, @NonNull Options options) throws IOException {
        Loader loader = new ByteBufferLoader() {
            @Override
            public ByteBuffer getByteBuffer() {
                source.position(0);
                return source;
            }
        };
        final FrameSeqDecoder decoder = new APNGDecoder(loader, null);
        return new FrameSeqDecoderResource(decoder, source.limit());
    }


}
