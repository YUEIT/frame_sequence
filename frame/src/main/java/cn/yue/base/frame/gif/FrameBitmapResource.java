package cn.yue.base.frame.gif;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.engine.Initializable;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.util.Preconditions;
import com.bumptech.glide.util.Util;

public class FrameBitmapResource implements Resource<Bitmap>, Initializable {
    private final Bitmap bitmap;


    /**
     * Returns a new {@link BitmapResource} wrapping the given {@link Bitmap} if the Bitmap is
     * non-null or null if the given Bitmap is null.
     *
     * @param bitmap A Bitmap.
     * @param bitmapPool A non-null {@link com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool}.
     */
    @Nullable
    public static FrameBitmapResource obtain(@Nullable Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        } else {
            return new FrameBitmapResource(bitmap);
        }
    }

    public FrameBitmapResource(@NonNull Bitmap bitmap) {
        this.bitmap = Preconditions.checkNotNull(bitmap, "Bitmap must not be null");

    }

    @NonNull
    @Override
    public Class<Bitmap> getResourceClass() {
        return Bitmap.class;
    }

    @NonNull
    @Override
    public Bitmap get() {
        return bitmap;
    }

    @Override
    public int getSize() {
        return Util.getBitmapByteSize(bitmap);
    }

    @Override
    public void recycle() {
//        bitmapPool.put(bitmap);
    }

    @Override
    public void initialize() {
        bitmap.prepareToDraw();
    }
}

