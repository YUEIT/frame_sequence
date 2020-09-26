package cn.yue.base.frame;

import android.content.Context;
import android.support.rastermill.GlideFrameSequenceDrawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import java.io.InputStream;

@GlideModule
public class GlideGifModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.prepend(Registry.BUCKET_GIF, InputStream.class, GlideFrameSequenceDrawable.class, new FrameSequenceStreamDecoder(glide.getBitmapPool()));
        registry.prepend(GlideFrameSequenceDrawable.class, new FrameSequenceEncoder());
    }
}
