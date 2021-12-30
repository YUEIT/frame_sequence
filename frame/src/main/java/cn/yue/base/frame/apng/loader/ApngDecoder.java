package cn.yue.base.frame.apng.loader;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;


/**
 * Description :
 * Created by yue on 2021/12/8
 */

public class ApngDecoder implements FrameDecoder {

    private static final String TAG = ApngDecoder.class.getSimpleName();
    private static final int INITIAL_FRAME_POINTER = -1;

    private final ApngFrameReader frameReader;
    private PackFrame mFrontFrame;
    private PackFrame mBackFrame;
    private int framePointer = INITIAL_FRAME_POINTER;

    public ApngDecoder(Context context, ApngWrapper apngWrapper, Bitmap.Config config) {
        frameReader = new ApngFrameReader(context, apngWrapper, config);
        mFrontFrame = new PackFrame(null, INITIAL_FRAME_POINTER);
        mBackFrame = new PackFrame(null, INITIAL_FRAME_POINTER);
        initFrame();
    }

    @Override
    public int getWidth() {
        return frameReader.getWidth();
    }

    @Override
    public int getHeight() {
        return frameReader.getHeight();
    }

    @Override
    public void advance() {
        framePointer = (framePointer + 1) % getFrameCount();
    }

    @Override
    public int getDelay(int n) {
        return (int) frameReader.getDelay(framePointer);
    }

    @Override
    public int getNextDelay() {
        return getDelay(getCurrentFrameIndex());
    }

    @Override
    public int getFrameCount() {
        return frameReader.getFrameCount();
    }

    @Override
    public int getCurrentFrameIndex() {
        return framePointer;
    }

    @Override
    public void resetFrameIndex() {
        framePointer = 0;
    }

    @Override
    public int getLoopCount() {
        return 0;
    }

    @Override
    public int getByteSize() {
        return 0;
    }

    @Nullable
    @Override
    public synchronized ApngFrame getNextFrame() {
        if (mFrontFrame.pointer != framePointer) {
            PackFrame temp = mFrontFrame;
            mFrontFrame = mBackFrame;
            mBackFrame = temp;
            loadFrame((framePointer + 1) % getFrameCount());
        }
        return mBackFrame.frame;
    }

    private void initFrame() {
        mFrontFrame.pointer = 0;
        frameReader.getFrame(0, mFrontFrame);
        mBackFrame.pointer = 1;
        frameReader.getFrame(1, mBackFrame);
    }

    private int loadPointer;

    private void loadFrame(int pointer) {
        if (loadPointer == pointer) {
            return;
        }
        loadPointer = pointer;
        mBackFrame.pointer = pointer;
        frameReader.getFrame(loadPointer, mBackFrame);
    }

    @Override
    public void clear() {
        frameReader.release();
        mFrontFrame.frame.recycle();
        mBackFrame.frame.recycle();
        mFrontFrame = null;
        mBackFrame = null;
        framePointer = INITIAL_FRAME_POINTER;
    }

    public static class PackFrame {
        ApngFrame frame;
        int pointer;

        public PackFrame(ApngFrame frame, int pointer) {
            this.frame = frame;
            this.pointer = pointer;
        }
    }
}
