package cn.yue.base.frame;

import android.support.rastermill.GlideFrameSequenceDrawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.resource.drawable.DrawableResource;

/**
 * Created by Administrator on 2018\11\18 0018.
 */

public class FrameSequenceDrawableResource extends DrawableResource<GlideFrameSequenceDrawable> {
    public FrameSequenceDrawableResource(GlideFrameSequenceDrawable drawable) {
        super(drawable);
    }

    @NonNull
    @Override
    public Class<GlideFrameSequenceDrawable> getResourceClass() {
        return GlideFrameSequenceDrawable.class;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public void recycle() {
        drawable.stop();
        drawable.recycle();
    }
}
