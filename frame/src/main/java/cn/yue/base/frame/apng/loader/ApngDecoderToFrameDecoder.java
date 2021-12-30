package cn.yue.base.frame.apng.loader;

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

public class ApngDecoderToFrameDecoder implements ResourceDecoder<ApngDecoder, ApngFrame> {

    public ApngDecoderToFrameDecoder() {
    }

    @Override
    public boolean handles(@NonNull ApngDecoder source, @NonNull Options options) throws IOException {
        return true;
    }

    @Nullable
    @Override
    public Resource<ApngFrame> decode(@NonNull ApngDecoder source, int width, int height, @NonNull Options options) throws IOException {
        ApngFrame frame = source.getNextFrame();
        return ApngFrameResource.obtain(frame);
    }
}
