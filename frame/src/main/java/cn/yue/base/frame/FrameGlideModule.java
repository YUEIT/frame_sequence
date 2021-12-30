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
import com.github.penfeizhou.animation.apng.APNGDrawable;
import com.github.penfeizhou.animation.decode.FrameSeqDecoder;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

import cn.yue.base.frame.anim.AnimDecoderToFrameDecoder;
import cn.yue.base.frame.anim.AnimFileWrapperFactory;
import cn.yue.base.frame.anim.AnimFrameDecoder;
import cn.yue.base.frame.anim.AnimFrameDrawable;
import cn.yue.base.frame.anim.AnimResourceWrapperFactory;
import cn.yue.base.frame.anim.AnimWrapper;
import cn.yue.base.frame.anim.AnimWrapperToDrawableDecoder;
import cn.yue.base.frame.anim.AnimZipWrapperFactory;
import cn.yue.base.frame.anim.Frame;
import cn.yue.base.frame.apng2.APNGDecoderToDrawableDecoder;
import cn.yue.base.frame.apng2.APNGDecoderToFrameDecoder;
import cn.yue.base.frame.apng2.ApngWrapperFactory;
import cn.yue.base.frame.apng2.ByteBufferAnimationDecoder;
import cn.yue.base.frame.apng2.StreamAnimationDecoder;
import cn.yue.base.frame.custom.FrameSequenceEncoder;
import cn.yue.base.frame.custom.FrameSequenceStreamDecoder;
import cn.yue.base.frame.custom.GlideFrameSequenceDrawable;
import cn.yue.base.frame.gif.ByteBitmapDecoder;
import cn.yue.base.frame.gif.ByteFrameDrawableDecoder;
import cn.yue.base.frame.gif.FrameDecoder;
import cn.yue.base.frame.gif.GifFrameDrawable;
import cn.yue.base.frame.gif.FrameEncoder;
import cn.yue.base.frame.gif.FrameResourceDecoder;
import cn.yue.base.frame.gif.StreamBitmapDecoder;
import cn.yue.base.frame.gif.StreamFrameDrawableDecoder;


@GlideModule
public class FrameGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {

        registerAnimFrame(context, glide, registry);
        registerApng(context, glide, registry);
    }

    private void registerGif(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.prepend(Registry.BUCKET_GIF, InputStream.class, GlideFrameSequenceDrawable.class, new FrameSequenceStreamDecoder(glide.getBitmapPool()));
        registry.prepend(GlideFrameSequenceDrawable.class, new FrameSequenceEncoder());

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
                .prepend(ByteBuffer.class, GifFrameDrawable.class, byteFrameDecoder)
                .prepend(InputStream.class, GifFrameDrawable.class, streamFrameDrawableDecoder)
                .prepend(GifFrameDrawable.class, new FrameEncoder())
                .prepend(FrameDecoder.class, Bitmap.class, frameResourceDecoder);
    }


    private void registerAnimFrame(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        AnimDecoderToFrameDecoder animDecoderToFrameDecoder = new AnimDecoderToFrameDecoder();
        AnimWrapperToDrawableDecoder animWrapperDecoder = new AnimWrapperToDrawableDecoder(context);
        registry.prepend(String.class, AnimWrapper.class, new AnimZipWrapperFactory(context));
        registry.prepend(File[].class, AnimWrapper.class, new AnimFileWrapperFactory());
        registry.prepend(Integer.class, AnimWrapper.class, new AnimResourceWrapperFactory(context));
        registry.prepend(AnimFrameDecoder.class, Frame.class, animDecoderToFrameDecoder);
        registry.prepend(AnimWrapper.class, AnimFrameDrawable.class, animWrapperDecoder);
        registry.append(AnimFrameDecoder.class, AnimFrameDecoder.class, UnitModelLoader.Factory.<AnimFrameDecoder>getInstance());

    }

    private void registerApng(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
//        ApngDecoderToFrameDecoder animDecoderToFrameDecoder = new ApngDecoderToFrameDecoder();
//        ApngWrapperToDrawableDecoder animWrapperDecoder = new ApngWrapperToDrawableDecoder(context);
//        registry.prepend(Integer.class, ApngWrapper.class, new ApngWrapperFactory(context));
//        registry.prepend(ApngDecoder.class, ApngFrame.class, animDecoderToFrameDecoder);
//        registry.prepend(ApngWrapper.class, ApngDrawable.class, animWrapperDecoder);
//        registry.append(ApngDecoder.class, ApngDecoder.class, UnitModelLoader.Factory.<ApngDecoder>getInstance());


        ByteBufferAnimationDecoder byteBufferAnimationDecoder = new ByteBufferAnimationDecoder();
        StreamAnimationDecoder streamAnimationDecoder = new StreamAnimationDecoder();
        APNGDecoderToDrawableDecoder apngDecoderToDrawableDecoder = new APNGDecoderToDrawableDecoder(streamAnimationDecoder);
        registry.prepend(Integer.class, FrameSeqDecoder.class, new ApngWrapperFactory(context));
        registry.prepend(InputStream.class, FrameSeqDecoder.class, streamAnimationDecoder);
        registry.prepend(ByteBuffer.class, FrameSeqDecoder.class, byteBufferAnimationDecoder);
        registry.prepend(InputStream.class, APNGDrawable.class, apngDecoderToDrawableDecoder);
        registry.prepend(FrameSeqDecoder.class, APNGDrawable.class, new APNGDecoderToFrameDecoder());
        registry.prepend(FrameSeqDecoder.class, FrameSeqDecoder.class, UnitModelLoader.Factory.<FrameSeqDecoder>getInstance());
    }

}
