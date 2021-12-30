package cn.yue.base.frame.anim;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;

public class AnimFrameDecoder implements IFrameDecoder {

    private static final String TAG = AnimFrameDecoder.class.getSimpleName();
    private static final int INITIAL_FRAME_POINTER = -1;

    private final AnimFrameReader frameReader;
    private PackFrame mFrontFrame;
    private PackFrame mBackFrame;
    private int framePointer = INITIAL_FRAME_POINTER;

    public AnimFrameDecoder(Context context, AnimWrapper animWrapper, Bitmap.Config config, boolean requireCache) {
        frameReader = new AnimFrameReader(context, animWrapper, config, requireCache);
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
        return (int)frameReader.getDelay(framePointer);
    }

    @Override
    public int getNextDelay() {
        return getDelay(framePointer);
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
        framePointer = INITIAL_FRAME_POINTER;
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
    public synchronized Frame getNextFrame() {
        if (mFrontFrame.pointer == framePointer) {
            return mFrontFrame.frame;
        } else if (mBackFrame.pointer == framePointer) {
            return mBackFrame.frame;
        } else {
            if (Math.abs(mFrontFrame.pointer - mBackFrame.pointer) >= 2) {
                if (mFrontFrame.pointer > mBackFrame.pointer) {
                    loadFrame(framePointer, mFrontFrame);
                    return mFrontFrame.frame;
                } else {
                    loadFrame(framePointer, mBackFrame);
                    return mBackFrame.frame;
                }
            } else {
                if (mFrontFrame.pointer > mBackFrame.pointer) {
                    loadFrame(framePointer, mBackFrame);
                    return mBackFrame.frame;
                } else {
                    loadFrame(framePointer, mFrontFrame);
                    return mFrontFrame.frame;
                }
            }
        }
    }

    private void initFrame() {
        mFrontFrame.pointer = 0;
        frameReader.getFrame(0, mFrontFrame);
        mBackFrame.pointer = 1;
        frameReader.getFrame(1, mBackFrame);
    }

    private int loadPointer;

    private void loadFrame(int pointer, PackFrame frame) {
        if (loadPointer == pointer) {
            return;
        }
        loadPointer = pointer;
        frame.pointer = pointer;
        frameReader.getFrame(loadPointer, frame);
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
        Frame frame;
        int pointer;
        public PackFrame(Frame frame, int pointer) {
            this.frame = frame;
            this.pointer = pointer;
        }
    }
}
