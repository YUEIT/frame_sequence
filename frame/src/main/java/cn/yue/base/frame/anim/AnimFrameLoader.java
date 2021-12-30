
package cn.yue.base.frame.anim;

import static com.bumptech.glide.request.RequestOptions.diskCacheStrategyOf;
import static com.bumptech.glide.request.RequestOptions.signatureOf;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.bumptech.glide.util.Preconditions;
import com.bumptech.glide.util.Synthetic;

import java.util.ArrayList;
import java.util.List;


public class AnimFrameLoader {

    private final IFrameDecoder frameDecoder;
    private final Handler handler;
    private final List<FrameCallback> callbacks = new ArrayList<>();

    @Synthetic
    final RequestManager requestManager;
    private RequestBuilder<Frame> requestBuilder;
    private final BitmapPool bitmapPool;

    private boolean isRunning;
    private boolean isLoadPending;
    private boolean startFromFirstFrame;
    private DelayTarget current;
    private boolean isCleared;
    private DelayTarget next;
    private Frame firstFrame;
    private Transformation<Frame> transformation;
    private DelayTarget pendingTarget;
    @Nullable
    private OnEveryFrameListener onEveryFrameListener;
    private int firstFrameSize;
    private int width;
    private int height;

    public interface FrameCallback {
        void onFrameReady();
    }

    AnimFrameLoader(
            Glide glide,
            IFrameDecoder frameDecoder,
            int width,
            int height,
            Transformation<Frame> transformation,
            Frame firstFrame) {
        this(
                glide.getBitmapPool(),
                Glide.with(glide.getContext()),
                frameDecoder,
                null /*handler*/,
                getRequestBuilder(Glide.with(glide.getContext()), width, height),
                transformation,
                firstFrame);
    }

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    AnimFrameLoader(
            BitmapPool bitmapPool,
            RequestManager requestManager,
            IFrameDecoder frameDecoder,
            Handler handler,
            RequestBuilder<Frame> requestBuilder,
            Transformation<Frame> transformation,
            Frame firstFrame) {
        this.requestManager = requestManager;
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper(), new FrameLoaderCallback());
        }
        this.bitmapPool = bitmapPool;
        this.handler = handler;
        this.requestBuilder = requestBuilder;
        this.frameDecoder = frameDecoder;

        setFrameTransformation(transformation, firstFrame);
    }

    void setFrameTransformation(Transformation<Frame> transformation, Frame firstFrame) {
        this.transformation = transformation;
        this.firstFrame = firstFrame;
        requestBuilder = requestBuilder.apply(new RequestOptions().transform(Frame.class, transformation));
        firstFrameSize = firstFrame.getSize();
        width = firstFrame.getWidth();
        height = firstFrame.getHeight();
    }

    Transformation<Frame> getFrameTransformation() {
        return transformation;
    }

    Frame getFirstFrame() {
        return firstFrame;
    }

    void subscribe(FrameCallback frameCallback) {
        if (isCleared) {
            throw new IllegalStateException("Cannot subscribe to a cleared frame loader");
        }
        if (callbacks.contains(frameCallback)) {
            throw new IllegalStateException("Cannot subscribe twice in a row");
        }
        boolean start = callbacks.isEmpty();
        callbacks.add(frameCallback);
        if (start) {
            start();
        }
    }

    void unsubscribe(FrameCallback frameCallback) {
        callbacks.remove(frameCallback);
        if (callbacks.isEmpty()) {
            stop();
        }
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }

    int getSize() {
        return frameDecoder.getByteSize() + firstFrameSize;
    }

    int getCurrentIndex() {
        return current != null ? current.index : -1;
    }

    int getCurrentFrameIndex() {
        return frameDecoder.getCurrentFrameIndex();
    }

    int getFrameCount() {
        return frameDecoder.getFrameCount();
    }

    int getLoopCount() {
        return frameDecoder.getLoopCount();
    }

    private void start() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        isCleared = false;

        loadNextFrame();
    }

    private void stop() {
        isRunning = false;
    }

    void clear() {
        callbacks.clear();
        recycleFirstFrame();
        stop();
        if (current != null) {
            requestManager.clear(current);
            current = null;
        }
        if (next != null) {
            requestManager.clear(next);
            next = null;
        }
        if (pendingTarget != null) {
            requestManager.clear(pendingTarget);
            pendingTarget = null;
        }
        frameDecoder.clear();
        isCleared = true;
        handler.removeCallbacksAndMessages(null);
    }

    Frame getCurrentFrame() {
        return current != null ? current.getResource() : firstFrame;
    }

    private void loadNextFrame() {
        if (!isRunning || isLoadPending) {
            return;
        }

        if (startFromFirstFrame) {
            Preconditions.checkArgument(
                    pendingTarget == null, "Pending target must be null when starting from the first frame");
            frameDecoder.resetFrameIndex();
            startFromFirstFrame = false;
        }

        if (pendingTarget != null) {
            DelayTarget temp = pendingTarget;
            pendingTarget = null;
            onFrameReady(temp);
            return;
        }

        isLoadPending = true;

        // Get the delay before incrementing the pointer because the delay indicates the amount of time
        // we want to spend on the current frame.
        int delay = frameDecoder.getNextDelay();
        long targetTime = SystemClock.uptimeMillis() + delay;

        frameDecoder.advance();
        int frameIndex = frameDecoder.getCurrentFrameIndex();
        next = new DelayTarget(handler, frameIndex, targetTime);


        next = new DelayTarget(handler, frameDecoder.getCurrentFrameIndex(), targetTime);

        requestBuilder.apply(signatureOf(getFrameSignature())).load(frameDecoder).into(next);
    }

    private void recycleFirstFrame() {
//        if (firstFrame != null) {
//            bitmapPool.put(firstFrame);
//            firstFrame = null;
//        }
    }

    void setNextStartFromFirstFrame() {
        if (isRunning) {
            return;
        }
        startFromFirstFrame = true;
        if (pendingTarget != null) {
            requestManager.clear(pendingTarget);
            pendingTarget = null;
        }
    }

    @VisibleForTesting
    void setOnEveryFrameReadyListener(@Nullable OnEveryFrameListener onEveryFrameListener) {
        this.onEveryFrameListener = onEveryFrameListener;
    }

    @VisibleForTesting
    void onFrameReady(DelayTarget delayTarget) {
        if (onEveryFrameListener != null) {
            onEveryFrameListener.onFrameReady();
        }
        isLoadPending = false;
        if (isCleared) {
            handler.obtainMessage(FrameLoaderCallback.MSG_CLEAR, delayTarget).sendToTarget();
            return;
        }
        // If we're not running, notifying here will recycle the frame that we might currently be
        // showing, which breaks things (see #2526). We also can't discard this frame because we've
        // already incremented the frame pointer and can't decode the same frame again. Instead we'll
        // just hang on to this next frame until start() or clear() are called.
        if (!isRunning) {
            pendingTarget = delayTarget;
            return;
        }

        if (delayTarget.getResource() != null) {

            recycleFirstFrame();
            DelayTarget previous = current;
            current = delayTarget;
            // The callbacks may unregister when onFrameReady is called, so iterate in reverse to avoid
            // concurrent modifications.
            for (int i = callbacks.size() - 1; i >= 0; i--) {
                FrameCallback cb = callbacks.get(i);
                cb.onFrameReady();
            }
            if (previous != null) {
                handler.obtainMessage(FrameLoaderCallback.MSG_CLEAR, previous).sendToTarget();
            }
        }
        loadNextFrame();
    }

    private class FrameLoaderCallback implements Handler.Callback {
        static final int MSG_DELAY = 1;
        static final int MSG_CLEAR = 2;

        @Synthetic
        FrameLoaderCallback() {
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_DELAY) {
                DelayTarget target = (DelayTarget) msg.obj;
                onFrameReady(target);
                return true;
            } else if (msg.what == MSG_CLEAR) {
                DelayTarget target = (DelayTarget) msg.obj;
                requestManager.clear(target);
            }
            return false;
        }
    }

    @VisibleForTesting
    static class DelayTarget extends CustomTarget<Frame> {
        private final Handler handler;
        @Synthetic
        final int index;
        private final long targetTime;
        private Frame resource;

        DelayTarget(Handler handler, int index, long targetTime) {
            this.handler = handler;
            this.index = index;
            this.targetTime = targetTime;
        }

        Frame getResource() {
            return resource;
        }

        @Override
        public void onResourceReady(
                @NonNull Frame resource, @Nullable Transition<? super Frame> transition) {
            this.resource = resource;
            Message msg = handler.obtainMessage(FrameLoaderCallback.MSG_DELAY, this);
            handler.sendMessageAtTime(msg, targetTime);
        }

        @Override
        public void onLoadCleared(@Nullable Drawable placeholder) {
            this.resource = null;
        }
    }

    private static RequestBuilder<Frame> getRequestBuilder(
            RequestManager requestManager, int width, int height) {
        return requestManager
                .as(Frame.class)
                .apply(
                        diskCacheStrategyOf(DiskCacheStrategy.NONE)
                                .useAnimationPool(true)
                                .skipMemoryCache(true)
                                .override(width, height));
    }


    private static Key getFrameSignature() {
        // Some devices seem to have crypto bugs that throw exceptions when you create a new UUID.
        // See #1510.
        return new ObjectKey(Math.random());
    }

    @VisibleForTesting
    interface OnEveryFrameListener {
        void onFrameReady();
    }
}


