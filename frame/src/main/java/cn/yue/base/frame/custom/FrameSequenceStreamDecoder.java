package cn.yue.base.frame.custom;

import android.graphics.Bitmap;
import android.support.rastermill.FrameSequence;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2018\11\18 0018.
 */

public class FrameSequenceStreamDecoder implements ResourceDecoder<InputStream, GlideFrameSequenceDrawable> {


    private BitmapPool bitmapPool;

    public FrameSequenceStreamDecoder(BitmapPool bitmapPool) {
        this.bitmapPool = bitmapPool;
    }

    @Override
    public boolean handles(@NonNull InputStream source, @NonNull Options options) throws IOException {
        return true;
    }

    @Nullable
    @Override
    public FrameSequenceDrawableResource decode(@NonNull InputStream source, int width, int height, @NonNull Options options) throws IOException {
        FrameSequence fs = FrameSequence.decodeStream(source);
        byte[] bytes = inputStreamToBytes(source);
        if (bytes == null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        GlideFrameSequenceDrawable drawable = new GlideFrameSequenceDrawable(fs, mProvider, byteBuffer);
        return new FrameSequenceDrawableResource(drawable);
    }

    private static byte[] inputStreamToBytes(InputStream is) {
        final int bufferSize = 16384;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(bufferSize);
        try {
            int nRead;
            byte[] data = new byte[bufferSize];
            while ((nRead = is.read(data)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException e) {
            return null;
        }
        return buffer.toByteArray();
    }

    // This provider is entirely unnecessary, just here to validate the acquire/release process
    private class CheckingProvider implements GlideFrameSequenceDrawable.BitmapProvider {

        @Override
        public Bitmap acquireBitmap(int minWidth, int minHeight) {
            return bitmapPool.getDirty(minWidth, minHeight, Bitmap.Config.ARGB_8888);
        }

        @Override
        public void releaseBitmap(Bitmap bitmap) {
            bitmapPool.put(bitmap);
        }
    }

    private CheckingProvider mProvider = new CheckingProvider();

}
