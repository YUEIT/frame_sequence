package cn.yue.base.frame.apng.loader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.engine.Initializable;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.util.Preconditions;


public class ApngFrameResource implements Resource<ApngFrame>, Initializable {
    private final ApngFrame frame;

    @Nullable
    public static ApngFrameResource obtain(@Nullable ApngFrame frame) {
        if (frame == null) {
            return null;
        } else {
            return new ApngFrameResource(frame);
        }
    }

    public ApngFrameResource(@NonNull ApngFrame frame) {
        this.frame = Preconditions.checkNotNull(frame, "Frame must not be null");

    }

    @NonNull
    @Override
    public Class<ApngFrame> getResourceClass() {
        return ApngFrame.class;
    }

    @NonNull
    @Override
    public ApngFrame get() {
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

