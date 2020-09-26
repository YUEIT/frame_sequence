package android.support.rastermill;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Description : FrameSequence 加载逻辑
 * Created by yue on 2020/6/5
 */
public class GlideFrameSequenceLoader {

    private static boolean debug = false;
    private static final String TAG = "FrameSequenceLoader";
    private static final Object sLock = new Object();
    private static final long MIN_DELAY_MS = 20L;
    private static final long DEFAULT_DELAY_MS = 100L;
    private static final int STATE_SCHEDULED = 1;
    private static final int STATE_DECODING = 2;
    private static final int STATE_WAITING_TO_SWAP = 3;
    private static final int STATE_READY_TO_SWAP = 4;


    private FrameSequence mFrameSequence;
    private FrameSequence.State mFrameSequenceState;
    private GlideFrameSequenceDrawable.BitmapProvider mBitmapProvider;
    private Bitmap mFrontBitmap;
    private Bitmap mBackBitmap;
    private int mNextFrameToDecode;
    private int width;
    private int height;
    private final List<FrameCallback> callbacks = new Vector<>(2);
    private boolean isCleared;
    private boolean isRunning;
    private boolean isLoadPending;
    private static HandlerThread sDecodingThread;
    private static Handler sDecodingThreadHandler;
    private final Handler handler;
    private Runnable mDecodeRunnable;
    private final Object mLock = new Object();
    private int mState;
    private long mLastSwap;
    private long mNextSwap;
    private ByteBuffer byteBuffer;

    public GlideFrameSequenceLoader(FrameSequence mFrameSequence, GlideFrameSequenceDrawable.BitmapProvider mBitmapProvider, ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        this.mFrameSequence = mFrameSequence;
        this.mBitmapProvider = mBitmapProvider;
        mFrameSequenceState = mFrameSequence.createState();
        width = mFrameSequence.getWidth();
        height = mFrameSequence.getHeight();
        mFrontBitmap = acquireAndValidateBitmap(mBitmapProvider, width, height);
        mBackBitmap = acquireAndValidateBitmap(mBitmapProvider, width, height);
        mFrameSequenceState.getFrame(0, mFrontBitmap, -1);
        mLastSwap = 0L;
        mNextFrameToDecode = -1;
        initializeDecodingThread();
        handler = new Handler(sDecodingThread.getLooper(), new FrameLoaderCallback());
        initRunnable();
    }

    private static Bitmap acquireAndValidateBitmap(GlideFrameSequenceDrawable.BitmapProvider bitmapProvider, int minWidth, int minHeight) {
        Bitmap bitmap = bitmapProvider.acquireBitmap(minWidth, minHeight);
        if (bitmap.getWidth() >= minWidth && bitmap.getHeight() >= minHeight && bitmap.getConfig() == Bitmap.Config.ARGB_8888) {
            return bitmap;
        } else {
            throw new IllegalArgumentException("Invalid bitmap provided");
        }
    }

    private static void initializeDecodingThread() {
        synchronized(sLock) {
            if (sDecodingThread == null) {
                sDecodingThread = new HandlerThread("FrameSequence decoding thread", Process.THREAD_PRIORITY_BACKGROUND);
                sDecodingThread.start();
                sDecodingThreadHandler = new Handler(sDecodingThread.getLooper());
            }
        }
    }

    public interface FrameCallback {
        void onFrameReady();
    }

    // 考虑多线程操作，下面几个方法不直接使用该类的内部变量，通过handle发送到解码的线程中处理加载流程
    /**
     * 监听
     * @param frameCallback
     */
    void subscribe(FrameCallback frameCallback) {
        if (debug) {
            Log.d(TAG, this + ": subscribe: " + frameCallback);
        }
        if (callbacks.contains(frameCallback)) {
            return;
        }
        callbacks.add(frameCallback);
        handler.sendEmptyMessage(FrameLoaderCallback.MSG_START);
    }

    /**
     * 取消监听
     * @param frameCallback
     */
    void unsubscribe(FrameCallback frameCallback) {
        if (debug) {
            Log.d(TAG, this + ": unsubscribe: " + frameCallback);
        }
        if (callbacks.contains(frameCallback)) {
            callbacks.remove(frameCallback);
            if (callbacks.isEmpty()) {
                handler.sendEmptyMessage(FrameLoaderCallback.MSG_STOP);
            }
        }
    }

    /**
     * 回收
     */
    void recycle() {
        handler.sendEmptyMessage(FrameLoaderCallback.MSG_CLEAR);
    }

    /**
     * 重置
     */
    void reset() {
        handler.sendEmptyMessage(FrameLoaderCallback.MSG_RESET);
    }

    private void start() {
        if (debug) {
            Log.d(TAG, this + ": start: ");
        }
        if (isRunning || isCleared) {
            return;
        }
        isRunning = true;
        isCleared = false;
        mState = STATE_READY_TO_SWAP;
        isLoadPending = false;
        handler.sendEmptyMessage(FrameLoaderCallback.MSG_LOAD_NEXT);
    }

    private void stop() {
        if (debug) {
            Log.d(TAG, this + ": stop: ");
        }
        isRunning = false;
        handler.removeMessages(FrameLoaderCallback.MSG_LOAD_NEXT);
        handler.removeMessages(FrameLoaderCallback.MSG_LOAD_FRAME);
    }

    private void clear() {
        if (debug) {
            Log.d(TAG, this + ": clear: ");
        }
        callbacks.clear();
        if (mFrontBitmap != null) {
            mBitmapProvider.releaseBitmap(mFrontBitmap);
        }
        if (mBackBitmap != null) {
            mBitmapProvider.releaseBitmap(mBackBitmap);
        }
        stop();
        isCleared = true;
        mState = 0;
        handler.removeCallbacksAndMessages(null);
    }

    private void resetFrame() {
        if (debug) {
            Log.d(TAG, this + ": resetFrame: ");
        }
        callbacks.clear();
        isRunning = false;
        mState = 0;
        handler.removeCallbacksAndMessages(null);
        mNextFrameToDecode = -1;
        mFrameSequenceState.getFrame(0, mFrontBitmap, -1);
    }

    private void initRunnable() {
        this.mDecodeRunnable = new Runnable() {
            public void run() {
                int nextFrame;
                Bitmap bitmap;
                synchronized(mLock) {
                    nextFrame = mNextFrameToDecode;
                    if (nextFrame < 0) {
                        return;
                    }
                    bitmap = mBackBitmap;
                    mState = STATE_DECODING;
                }

                int lastFrame = nextFrame - 2;
                boolean exceptionDuringDecode = false;
                long invalidateTimeMs = 0L;

                try {
                    //取帧 放入backBitmap
                    invalidateTimeMs = mFrameSequenceState.getFrame(nextFrame, bitmap, lastFrame);
                } catch (Exception var12) {
                    if (debug) {
                        Log.e(TAG, "exception during decode: " + var12);
                    }
                    exceptionDuringDecode = true;
                }

                if (invalidateTimeMs < MIN_DELAY_MS) {
                    invalidateTimeMs = DEFAULT_DELAY_MS;
                }
                boolean schedule = false;
                synchronized(mLock) {
                    if (mNextFrameToDecode >= 0 && mState == STATE_DECODING) {
                        schedule = true;
                        mNextSwap = exceptionDuringDecode ? Long.MAX_VALUE : invalidateTimeMs + mLastSwap;
                        mState = STATE_WAITING_TO_SWAP;
                    }
                }
                if (schedule) {
                    //mNextSwap
                    long delay = mNextSwap - SystemClock.uptimeMillis();
                    if (delay < 0L) {
                        delay = 0;
                    }
                    handler.sendEmptyMessageDelayed(FrameLoaderCallback.MSG_LOAD_FRAME, delay);
                }
            }
        };
    }

    private void onLoadFrame() {
        boolean invalidate = false;
        synchronized(this.mLock) {
            if (this.mNextFrameToDecode >= 0 && this.mState == STATE_WAITING_TO_SWAP) {
                this.mState = STATE_READY_TO_SWAP;
                invalidate = true;
            }
        }
        if (invalidate) {
            onFrameReady();
        }
    }

    /**
     * 帧已取，准备加载
     */
    private void onFrameReady() {
        isLoadPending = false;
        if (isCleared) {
            handler.obtainMessage(FrameLoaderCallback.MSG_CLEAR, null).sendToTarget();
            return;
        }
        if (!isRunning) {
            return;
        }
        // 将已取到的最新帧放入 frontBitmap，已加载过的放入backBitmap，后续从native取的帧会替换掉backBitmap
        Bitmap tmp = mBackBitmap;
        this.mBackBitmap = this.mFrontBitmap;
        this.mFrontBitmap = tmp;
        try {
            for (Iterator<FrameCallback> iterator = callbacks.iterator(); iterator.hasNext(); ) {
                FrameCallback cb = iterator.next();
                // 通知所有观察者，表明可以获取bitmap 刷新了
                cb.onFrameReady();
            }
        } catch (Exception e) {
            // 多线程环境下运行循环体内size可能会被更改
            // 一般是在停止加载时，所以这里丢帧不会影响UI展示
            e.printStackTrace();
        }
        loadNextFrame();
    }

    /**
     *
     * 取下一帧 draw -> draw
     */
    private void loadNextFrame() {
        if (debug) {
            Log.d(TAG, this + ": loadNextFrame: " + mNextFrameToDecode);
        }
        if (!isRunning || isLoadPending) {
            return;
        }
        isLoadPending = true;
        synchronized(this.mLock) {
            if (mState == STATE_WAITING_TO_SWAP && mNextSwap - SystemClock.uptimeMillis() <= 0L) {
                mState = STATE_READY_TO_SWAP;
            }
            if (isRunning && mState == STATE_READY_TO_SWAP) {
                mLastSwap = SystemClock.uptimeMillis();
                mState = STATE_SCHEDULED;
                //修改帧数
                mNextFrameToDecode = (mNextFrameToDecode + 1) % mFrameSequence.getFrameCount();
                sDecodingThreadHandler.post(this.mDecodeRunnable);
            }
        }
    }

    Bitmap getCurrentFrame() {
        return mFrontBitmap;
    }

    Bitmap getFrontBitmap() {
        return mFrontBitmap;
    }

    Bitmap getBackBitmap() {
        return mBackBitmap;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    int getFrameCount() {
        return mFrameSequence.getFrameCount();
    }

    int getCurrentIndex() {
        return mNextFrameToDecode;
    }

    int getDefaultLoopCount() {
        return mFrameSequence.getDefaultLoopCount();
    }

    boolean isOpaque() {
        return mFrameSequence.isOpaque();
    }

    ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    private class FrameLoaderCallback implements Handler.Callback {
        static final int MSG_START = 100;
        static final int MSG_LOAD_NEXT = 101;
        static final int MSG_STOP = 102;
        static final int MSG_CLEAR = 103;
        static final int MSG_RESET = 104;
        static final int MSG_LOAD_FRAME = 105;

        FrameLoaderCallback() {}

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START:
                    start();
                    return true;
                case MSG_LOAD_NEXT:
                    loadNextFrame();
                    return true;
                case MSG_STOP:
                    stop();
                    return true;
                case MSG_CLEAR:
                    clear();
                    return true;
                case MSG_RESET:
                    resetFrame();
                    return true;
                case MSG_LOAD_FRAME:
                    onLoadFrame();
                    return true;
                default:
                    return false;
            }
        }
    }

    public static void setDebug(boolean isDebug) {
        debug = isDebug;
    }
}
