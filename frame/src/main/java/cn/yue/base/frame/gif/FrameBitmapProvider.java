package cn.yue.base.frame.gif;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;

public class FrameBitmapProvider implements FrameDecoder.BitmapProvider{

    private final BitmapPool bitmapPool;
    @Nullable
    private final ArrayPool arrayPool;

    /**
     * Constructs an instance without a shared byte array pool. Byte arrays will be always constructed
     * when requested.
     */
    public FrameBitmapProvider(BitmapPool bitmapPool) {
        this(bitmapPool, /*arrayPool=*/ null);
    }

    /** Constructs an instance with a shared array pool. Arrays will be reused where possible. */
    // Public API.
    public FrameBitmapProvider(BitmapPool bitmapPool, @Nullable ArrayPool arrayPool) {
        this.bitmapPool = bitmapPool;
        this.arrayPool = arrayPool;
    }

    @NonNull
    @Override
    public Bitmap obtain(int width, int height, @NonNull Bitmap.Config config) {
        return bitmapPool.getDirty(width, height, config);
    }

    @Override
    public void release(@NonNull Bitmap bitmap) {
        bitmapPool.put(bitmap);
    }

    @NonNull
    @Override
    public byte[] obtainByteArray(int size) {
        if (arrayPool == null) {
            return new byte[size];
        }
        return arrayPool.get(size, byte[].class);
    }

    @Override
    public void release(@NonNull byte[] bytes) {
        if (arrayPool == null) {
            return;
        }
        arrayPool.put(bytes);
    }

    @NonNull
    @Override
    public int[] obtainIntArray(int size) {
        if (arrayPool == null) {
            return new int[size];
        }
        return arrayPool.get(size, int[].class);
    }

    @SuppressWarnings("PMD.UseVarargs")
    @Override
    public void release(@NonNull int[] array) {
        if (arrayPool == null) {
            return;
        }
        arrayPool.put(array);
    }
}
