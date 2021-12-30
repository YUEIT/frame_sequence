package cn.yue.base.frame.gif;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.Initializable;
import com.bumptech.glide.load.resource.drawable.DrawableResource;

public class FrameDrawableResource extends DrawableResource<GifFrameDrawable> implements Initializable {
    // Public API.
    @SuppressWarnings("WeakerAccess")
    public FrameDrawableResource(GifFrameDrawable drawable) {
        super(drawable);
    }

    @NonNull
    @Override
    public Class<GifFrameDrawable> getResourceClass() {
        return GifFrameDrawable.class;
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

