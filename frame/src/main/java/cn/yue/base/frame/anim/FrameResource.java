package cn.yue.base.frame.anim;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.engine.Initializable;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.util.Preconditions;

public class FrameResource implements Resource<Frame>, Initializable {
    private final Frame frame;

    @Nullable
    public static FrameResource obtain(@Nullable Frame frame) {
        if (frame == null) {
            return null;
        } else {
            return new FrameResource(frame);
        }
    }

    public FrameResource(@NonNull Frame frame) {
        this.frame = Preconditions.checkNotNull(frame, "Frame must not be null");

    }

    @NonNull
    @Override
    public Class<Frame> getResourceClass() {
        return Frame.class;
    }

    @NonNull
    @Override
    public Frame get() {
        return frame;
    }

    @Override
    public int getSize() {
        return frame.getSize();
    }

    @Override
    public void recycle() {
//        bitmapPool.put(bitmap);
    }

    @Override
    public void initialize() {
        frame.initialize();
    }
}

