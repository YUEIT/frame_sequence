package android.support.rastermill;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Description :
 * Created by yue on 2020/6/5
 */
public class GlideFrameSequenceDrawable extends Drawable implements Animatable, Runnable, GlideFrameSequenceLoader.FrameCallback {
    
    private static BitmapProvider sAllocatingBitmapProvider = new BitmapProvider() {
        public Bitmap acquireBitmap(int minWidth, int minHeight) {
            return Bitmap.createBitmap(minWidth, minHeight, Config.ARGB_8888);
        }

        public void releaseBitmap(Bitmap bitmap) {
        }
    };
    public static final int LOOP_FINITE = 1;
    public static final int LOOP_INF = 2;
    public static final int LOOP_DEFAULT = 3;
    /** @deprecated */
    @Deprecated
    public static final int LOOP_ONCE = 1;
    private final Paint mPaint;
    private final Rect mSrcRect;
    private int mCurrentLoop;
    private int mLoopBehavior;
    private int mLoopCount;
    private OnFinishedListener mOnFinishedListener;
    private Runnable mFinishedCallbackRunnable;

    private GifState gifState;
    private boolean isStarted;
    private boolean isVisible = true;
    private boolean isRunning;
    private boolean isRecycled;

    public void setOnFinishedListener(OnFinishedListener onFinishedListener) {
        mOnFinishedListener = onFinishedListener;
    }

    public void setLoopBehavior(int loopBehavior) {
        mLoopBehavior = loopBehavior;
    }

    public void setLoopCount(int loopCount) {
        mLoopCount = loopCount;
    }

    public GlideFrameSequenceDrawable(FrameSequence frameSequence) {
        this(frameSequence, sAllocatingBitmapProvider, null);
    }

    public GlideFrameSequenceDrawable(FrameSequence frameSequence, BitmapProvider bitmapProvider, ByteBuffer byteBuffer) {
        isRecycled = false;
        mLoopBehavior = LOOP_INF;
        mLoopCount = 1;
        if (frameSequence != null && bitmapProvider != null) {
            int width = frameSequence.getWidth();
            int height = frameSequence.getHeight();
            if (gifState == null) {
                GlideFrameSequenceLoader frameLoader = new GlideFrameSequenceLoader(frameSequence, bitmapProvider, byteBuffer);
                gifState = new GifState(frameLoader);
            }
            mSrcRect = new Rect(0, 0, width, height);
            mPaint = new Paint();
            mPaint.setFilterBitmap(true);
        } else {
            throw new IllegalArgumentException();
        }
        mFinishedCallbackRunnable = new Runnable() {
            public void run() {
                if (mOnFinishedListener != null) {
                    mOnFinishedListener.onFinished(GlideFrameSequenceDrawable.this);
                }
            }
        };
    }

    public GlideFrameSequenceDrawable(GifState gifState) {
        this.gifState = gifState;
        isRecycled = false;
        mLoopBehavior = LOOP_INF;
        mLoopCount = 1;
        mSrcRect = new Rect(0, 0, gifState.frameLoader.getWidth(), gifState.frameLoader.getHeight());
        mPaint = new Paint();
        mPaint.setFilterBitmap(true);
        mFinishedCallbackRunnable = new Runnable() {
            public void run() {
                if (mOnFinishedListener != null) {
                    mOnFinishedListener.onFinished(GlideFrameSequenceDrawable.this);
                }
            }
        };
    }

    @Override
    public void draw(Canvas canvas) {
        if (isRecycled) {
            return;
        }
        Bitmap currentFrame = gifState.frameLoader.getCurrentFrame();
        canvas.drawBitmap(currentFrame, mSrcRect, getBounds(), mPaint);
        // setImageDrawable 不会调用start
        if (!isStarted) {
            start();
        }
    }

    @Override
    public void start() {
        isStarted = true;
        resetLoopCount();
        if (isVisible) {
            startRunning();
        }
    }

    @Override
    public void stop() {
        isStarted = false;
        stopRunning();
    }

    private void startRunning() {
        if (gifState.frameLoader.getFrameCount() == 1) {
            scheduleSelf(this, 0L);
        } else if (!isRunning) {
            isRunning = true;
            gifState.frameLoader.subscribe(this);
            scheduleSelf(this, 0L);
        }
    }

    private void stopRunning() {
        isRunning = false;
        gifState.frameLoader.unsubscribe(this);
    }

    @Override
    public boolean isRunning() {
        return !isRecycled && isRunning;
    }

    /**
     * 解码反馈 （解码线程中运行）
     */
    @Override
    public void onFrameReady() {
        if (findCallback() == null) {
            stop();
            scheduleSelf(this, 0L);
            return;
        }
        // 刷新自身 回调draw
        scheduleSelf(this, 0L);
        //loop 逻辑
        if (getFrameIndex() == getFrameCount() - 1) {
            ++mCurrentLoop;
        }
        boolean continueLooping = true;
        if (getFrameIndex() == getFrameCount() - 1) {
            ++mCurrentLoop;
            if (mLoopBehavior == LOOP_FINITE && mCurrentLoop == mLoopCount
                    || mLoopBehavior == LOOP_DEFAULT && mCurrentLoop == getDefaultLoopCount()) {
                continueLooping = false;
            }
        }
        if (!continueLooping) {
            scheduleSelf(mFinishedCallbackRunnable, 0L);
            stop();
        }
    }

    @Override
    public void run() {
        invalidateSelf();
    }

    public int getFrameIndex() {
        return gifState.frameLoader.getCurrentIndex();
    }

    public int getFrameCount() {
        return gifState.frameLoader.getFrameCount();
    }

    public int getDefaultLoopCount() {
        return gifState.frameLoader.getDefaultLoopCount();
    }

    private void resetLoopCount() {
        mCurrentLoop = 0;
    }

    private Callback findCallback() {
        Callback callback = getCallback();
        while (callback instanceof Drawable) {
            callback = ((Drawable) callback).getCallback();
        }
        return callback;
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        isVisible = visible;
        if (!visible) {
            stopRunning();
        } else if (isStarted) {
           startRunning();
        }
        return super.setVisible(visible, restart);
    }

    public void setFilterBitmap(boolean filter) {
        mPaint.setFilterBitmap(filter);
    }

    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getIntrinsicWidth() {
        return gifState.frameLoader.getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return gifState.frameLoader.getHeight();
    }

    public int getOpacity() {
        return gifState.frameLoader.isOpaque() ? PixelFormat.OPAQUE : PixelFormat.TRANSPARENT;
    }

    public ByteBuffer getByteBuffer() {
        return gifState.frameLoader.getByteBuffer();
    }

    public interface BitmapProvider {
        Bitmap acquireBitmap(int var1, int var2);

        void releaseBitmap(Bitmap var1);
    }

    public interface OnFinishedListener {
        void onFinished(GlideFrameSequenceDrawable var1);
    }

    @Override
    public ConstantState getConstantState() {
        return gifState;
    }

    public void recycle() {
        isRecycled = true;
        gifState.frameLoader.recycle();
    }

    public void reset() {
        isStarted = false;
        isRunning = false;
        gifState.frameLoader.reset();
    }

    /**
     * 缓存逻辑，通过该类每次返回一个 newDrawable，且共同持有FrameSequenceLoader对象
     */
    static final class GifState extends ConstantState {

        GlideFrameSequenceLoader frameLoader;
        GifState(GlideFrameSequenceLoader frameLoader) {
            this.frameLoader = frameLoader;
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return newDrawable();
        }

        @Override
        public Drawable newDrawable() {
            return new GlideFrameSequenceDrawable(this);
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }
    }
}
