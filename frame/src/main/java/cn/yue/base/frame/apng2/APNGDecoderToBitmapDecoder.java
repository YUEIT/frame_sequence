package cn.yue.base.frame.apng2;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.github.penfeizhou.animation.decode.FrameSeqDecoder;

import java.io.IOException;


/**
 * Description :
 * Created by yue on 2021/11/29
 */

public class APNGDecoderToBitmapDecoder implements ResourceDecoder<FrameSeqDecoder, Bitmap> {

    private BitmapPool bitmapPool;
    public APNGDecoderToBitmapDecoder(BitmapPool bitmapPool) {
        this.bitmapPool = bitmapPool;
    }

    @Override
    public boolean handles(@NonNull FrameSeqDecoder source, @NonNull Options options) throws IOException {
        return true;
    }

    @Nullable
    @Override
    public Resource<Bitmap> decode(@NonNull FrameSeqDecoder source, int width, int height, @NonNull Options options) throws IOException {
        try {
            Bitmap bitmap = source.getFrameBitmap(0);
            return BitmapResource.obtain(bitmap, bitmapPool);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
