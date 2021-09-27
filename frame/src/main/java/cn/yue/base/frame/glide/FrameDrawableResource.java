package cn.yue.base.frame.glide;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.Initializable;
import com.bumptech.glide.load.resource.drawable.DrawableResource;

public class FrameDrawableResource extends DrawableResource<FrameDrawable> implements Initializable {
    // Public API.
    @SuppressWarnings("WeakerAccess")
    public FrameDrawableResource(FrameDrawable drawable) {
        super(drawable);
    }

    @NonNull
    @Override
    public Class<FrameDrawable> getResourceClass() {
        return FrameDrawable.class;
    }

    @Override
    public int getSize() {
        return drawable.getSize();
    }

    @Override
    public void recycle() {
        drawable.stop();
        drawable.recycle();
    }

    @Override
    public void initialize() {

    }
}

