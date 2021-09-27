package cn.yue.base.frame.custom;

import cn.yue.base.frame.custom.GlideFrameSequenceDrawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.EncodeStrategy;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceEncoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.util.ByteBufferUtil;

import java.io.File;
import java.io.IOException;

/**
 * Description : 编码，gif缓存
 * Created by yue on 2020/6/5
 */
class FrameSequenceEncoder implements ResourceEncoder<GlideFrameSequenceDrawable> {

    @NonNull
    @Override
    public EncodeStrategy getEncodeStrategy(@NonNull Options options) {
        return EncodeStrategy.SOURCE;
    }

    @Override
    public boolean encode(@NonNull Resource<GlideFrameSequenceDrawable> data,
                          @NonNull File file, @NonNull Options options) {
        GlideFrameSequenceDrawable drawable = data.get();
        boolean success = false;
        try {
            ByteBufferUtil.toFile(drawable.getByteBuffer(), file);
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }
}
