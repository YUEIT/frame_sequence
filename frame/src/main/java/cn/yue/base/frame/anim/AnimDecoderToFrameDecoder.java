package cn.yue.base.frame.anim;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;

import java.io.IOException;

/**
 * Description :
 * Created by yue on 2021/11/29
 */

public class AnimDecoderToFrameDecoder implements ResourceDecoder<AnimFrameDecoder, Frame> {

    public AnimDecoderToFrameDecoder() {
    }

    @Override
    public boolean handles(@NonNull AnimFrameDecoder source, @NonNull Options options) throws IOException {
        return true;
    }

    @Nullable
    @Override
    public Resource<Frame> decode(@NonNull AnimFrameDecoder source, int width, int height, @NonNull Options options) throws IOException {
        Frame frame = source.getNextFrame();
        return FrameResource.obtain(frame);
    }
}
