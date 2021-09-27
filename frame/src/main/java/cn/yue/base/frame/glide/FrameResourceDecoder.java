package cn.yue.base.frame.glide;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;

public class FrameResourceDecoder implements ResourceDecoder<FrameDecoder, Bitmap> {

    private final BitmapPool bitmapPool;

    public FrameResourceDecoder(BitmapPool bitmapPool) {
        this.bitmapPool = bitmapPool;
    }

    public FrameResourceDecoder(Context context) {
        this(Glide.get(context).getBitmapPool());
    }

    @Override
    public boolean handles(@NonNull FrameDecoder source, @NonNull Options options) {
        return true;
    }

    @Override
    public Resource<Bitmap> decode(
            @NonNull FrameDecoder source, int width, int height, @NonNull Options options) {
        Bitmap bitmap = source.getNextFrame();
        return FrameBitmapResource.obtain(bitmap);
    }
}
