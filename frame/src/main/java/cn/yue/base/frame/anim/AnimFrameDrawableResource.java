package cn.yue.base.frame.anim;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.Initializable;
import com.bumptech.glide.load.resource.drawable.DrawableResource;


public class AnimFrameDrawableResource extends DrawableResource<AnimFrameDrawable> implements Initializable {

    public AnimFrameDrawableResource(AnimFrameDrawable drawable) {
        super(drawable);
    }

    @NonNull
    @Override
    public Class<AnimFrameDrawable> getResourceClass() {
        return AnimFrameDrawable.class;
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

