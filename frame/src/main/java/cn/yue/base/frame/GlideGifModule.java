package cn.yue.base.frame;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.ImageHeaderParser;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.model.UnitModelLoader;
import com.bumptech.glide.load.resource.bitmap.BitmapDrawableDecoder;
import com.bumptech.glide.module.AppGlideModule;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

import cn.yue.base.frame.glide.ByteBitmapDecoder;
import cn.yue.base.frame.glide.FrameDecoder;
import cn.yue.base.frame.glide.FrameDrawable;
import cn.yue.base.frame.glide.FrameEncoder;
import cn.yue.base.frame.glide.FrameResourceDecoder;
import cn.yue.base.frame.glide.StreamBitmapDecoder;
import cn.yue.base.frame.glide.StreamFrameDrawableDecoder;
import cn.yue.base.frame.glide.ByteFrameDrawableDecoder;

@GlideModule
public class GlideGifModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
//        registry.prepend(Registry.BUCKET_GIF, InputStream.class, GlideFrameSequenceDrawable.class, new FrameSequenceStreamDecoder(glide.getBitmapPool()));
//        registry.prepend(GlideFrameSequenceDrawable.class, new FrameSequenceEncoder());

        final Resources resources = context.getResources();
        List<ImageHeaderParser> parsers = registry.getImageHeaderParsers();
        BitmapPool bitmapPool = glide.getBitmapPool();
        ArrayPool arrayPool = glide.getArrayPool();

        ByteFrameDrawableDecoder byteFrameDecoder = new ByteFrameDrawableDecoder(context, parsers, bitmapPool, arrayPool);
        StreamFrameDrawableDecoder streamFrameDrawableDecoder = new StreamFrameDrawableDecoder(context, parsers, arrayPool, byteFrameDecoder);

        ByteBitmapDecoder byteBitmapDecoder = new ByteBitmapDecoder(context, parsers, bitmapPool, arrayPool);
        StreamBitmapDecoder streamBitmapDecoder = new StreamBitmapDecoder(context, parsers, arrayPool, byteBitmapDecoder);

        FrameResourceDecoder frameResourceDecoder = new FrameResourceDecoder(bitmapPool);

        registry
                /* Bitmaps for static webp images */
                .prepend(Registry.BUCKET_BITMAP, ByteBuffer.class, Bitmap.class, byteBitmapDecoder)
                .prepend(Registry.BUCKET_BITMAP, InputStream.class, Bitmap.class, streamBitmapDecoder)
                /* BitmapDrawables for static webp images */
                .prepend(
                        Registry.BUCKET_BITMAP_DRAWABLE,
                        ByteBuffer.class,
                        BitmapDrawable.class,
                        new BitmapDrawableDecoder<>(resources, byteBitmapDecoder))
                .prepend(
                        Registry.BUCKET_BITMAP_DRAWABLE,
                        InputStream.class,
                        BitmapDrawable.class,
                        new BitmapDrawableDecoder<>(resources, streamBitmapDecoder))
                .append(
                        FrameDecoder.class, FrameDecoder.class, UnitModelLoader.Factory.<FrameDecoder>getInstance())
                /* Animated webp images */
                .prepend(ByteBuffer.class, FrameDrawable.class, byteFrameDecoder)
                .prepend(InputStream.class, FrameDrawable.class, streamFrameDrawableDecoder)
                .prepend(FrameDrawable.class, new FrameEncoder())
                .prepend(FrameDecoder.class, Bitmap.class, frameResourceDecoder);
    }
}
