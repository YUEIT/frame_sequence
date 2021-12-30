package cn.yue.base.frame.apng.loader;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.Initializable;
import com.bumptech.glide.load.resource.drawable.DrawableResource;

/**
 * Description :
 * Created by yue on 2021/12/8
 */

public class ApngDrawableResource extends DrawableResource<ApngDrawable> implements Initializable {

    public ApngDrawableResource(ApngDrawable drawable) {
        super(drawable);
    }

    @NonNull
    @Override
    public Class<ApngDrawable> getResourceClass() {
        return ApngDrawable.class;
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
