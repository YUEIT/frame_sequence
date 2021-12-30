package cn.yue.base.frame.gif;

import android.graphics.Bitmap;
import android.support.rastermill.FrameSequence;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class SequenceFrameDecoder implements FrameDecoder{

    private static final String TAG = SequenceFrameDecoder.class.getSimpleName();
    private static final int INITIAL_FRAME_POINTER = -1;

    private final FrameSequence frameSequence;
    private final FrameSequence.State mFrameSequenceState;
    private final FrameDecoder.BitmapProvider provider;
    private ByteBuffer rawData;
    private PackBitmap mFrontBitmap;
    private PackBitmap mBackBitmap;
    private int framePointer = INITIAL_FRAME_POINTER;
    private final Map<Integer, Integer> delayCache = new HashMap<>();

    public SequenceFrameDecoder(
            @NonNull FrameDecoder.BitmapProvider provider, ByteBuffer rawData, Bitmap.Config config) {
        this.provider = provider;
        this.rawData = rawData;
        frameSequence = FrameSequence.decodeByteBuffer(rawData);
        mFrameSequenceState = frameSequence.createState();
        mFrontBitmap = new PackBitmap(acquireAndValidateBitmap(provider, config), INITIAL_FRAME_POINTER);
        mBackBitmap = new PackBitmap(acquireAndValidateBitmap(provider, config), INITIAL_FRAME_POINTER);
        initFrame();
    }

    private Bitmap acquireAndValidateBitmap(FrameDecoder.BitmapProvider provider, Bitmap.Config config) {
        int width = frameSequence.getWidth();
        int height = frameSequence.getHeight();
        return provider.obtain(width, height, config);
    }

    @Override
    public int getWidth() {
        return frameSequence.getWidth();
    }

    @Override
    public int getHeight() {
        return frameSequence.getHeight();
    }

    @NonNull
    @Override
    public ByteBuffer getData() {
        return rawData;
    }

    @Override
    public void advance() {
        framePointer = (framePointer + 1) % getFrameCount();
    }

    @Override
    public int getDelay(int n) {
        Integer currentDelay = delayCache.get(n);
        if (currentDelay != null && currentDelay > 0) {
            return currentDelay;
        }
        currentDelay = (int)mFrameSequenceState.getDelay(framePointer, framePointer -1);
        delayCache.put(n, currentDelay);
        return currentDelay;
    }

    @Override
    public int getNextDelay() {
        return getDelay(getCurrentFrameIndex());
    }

    @Override
    public int getFrameCount() {
        return frameSequence.getFrameCount();
    }

    @Override
    public int getCurrentFrameIndex() {
        return framePointer;
    }

    @Override
    public void resetFrameIndex() {
        framePointer = INITIAL_FRAME_POINTER;
    }

    @Override
    public int getLoopCount() {
        return 1;
    }

    @Override
    public int getByteSize() {
        return rawData.limit();
    }

    @Nullable
    @Override
    public synchronized Bitmap getNextFrame() {
        if (mFrontBitmap.pointer != framePointer) {
            PackBitmap temp = mFrontBitmap;
            mFrontBitmap = mBackBitmap;
            mBackBitmap = temp;
            loadFrame((framePointer + 1) % getFrameCount());
        }
        return mFrontBitmap.bitmap;
    }

    private void initFrame() {
        mFrontBitmap.pointer = 0;
        mFrameSequenceState.getFrame(0, mFrontBitmap.bitmap, -1);
        mBackBitmap.pointer = 1;
        mFrameSequenceState.getFrame(1, mBackBitmap.bitmap, 0);
    }

    private int loadPointer;

    private void loadFrame(int pointer) {
        if (loadPointer == pointer) {
            return ;
        }
        loadPointer = pointer;
        mBackBitmap.pointer = pointer;
        mFrameSequenceState.getFrame(loadPointer, mBackBitmap.bitmap, loadPointer - 1);
    }

    @Override
    public void clear() {
        provider.release(mFrontBitmap.bitmap);
        provider.release(mBackBitmap.bitmap);
        mFrontBitmap = null;
        mBackBitmap = null;
        rawData = null;
        delayCache.clear();
    }

    static class PackBitmap {
        Bitmap bitmap;
        int pointer;
        public PackBitmap(Bitmap bitmap, int pointer) {
            this.bitmap = bitmap;
            this.pointer = pointer;
        }
    }
}
