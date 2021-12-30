package cn.yue.base.frame.apng2;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.Resource;
import com.github.penfeizhou.animation.decode.FrameSeqDecoder;

/**
 * Description :
 * Created by yue on 2021/12/15
 */

public class FrameSeqDecoderResource implements Resource<FrameSeqDecoder> {
    private final FrameSeqDecoder decoder;
    private final int size;

    FrameSeqDecoderResource(FrameSeqDecoder decoder, int size) {
        this.decoder = decoder;
        this.size = size;
    }

    @NonNull
    @Override
    public Class<FrameSeqDecoder> getResourceClass() {
        return FrameSeqDecoder.class;
    }

    @NonNull
    @Override
    public FrameSeqDecoder get() {
        return this.decoder;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public void recycle() {
        this.decoder.stop();
    }
}