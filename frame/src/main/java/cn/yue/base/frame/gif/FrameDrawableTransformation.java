package cn.yue.base.frame.gif;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;

import java.security.MessageDigest;

public class FrameDrawableTransformation implements Transformation<GifFrameDrawable> {

    public FrameDrawableTransformation() { }

    @NonNull
    @Override
    public Resource<GifFrameDrawable> transform(
            @NonNull Context context,
            @NonNull Resource<GifFrameDrawable> resource,
            int outWidth,
            int outHeight) {
//        drawable.setFrameTransformation(wrapped, transformedFrame);
        return resource;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

    }
}

