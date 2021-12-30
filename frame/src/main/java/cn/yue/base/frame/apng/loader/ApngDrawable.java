package cn.yue.base.frame.apng.loader;

import static cn.yue.base.frame.anim.IFrameDecoder.TOTAL_ITERATION_COUNT_FOREVER;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.util.Preconditions;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Description :
 * Created by yue on 2021/12/8
 */

public class ApngDrawable extends Drawable
        implements ApngLoader.FrameCallback, Animatable, Animatable2Compat {
    /** A constant indicating that an animated drawable should loop continuously. */
    // Public API.
    @SuppressWarnings("WeakerAccess")
    public static final int LOOP_FOREVER = -1;
    /**
     * A constant indicating that an animated drawable should loop for its default number of times.
     * For animated, this constant indicates the frame should use the netscape loop count if
     * present.
     */
    // Public API.
    @SuppressWarnings("WeakerAccess")
    public static final int LOOP_INTRINSIC = 0;

    private static final int GRAVITY = Gravity.FILL;

    private final ApngDrawable.FrameState state;
    /** True if the drawable is currently animating. */
    private boolean isRunning;
    /** True if the drawable should animate while visible. */
    private boolean isStarted;
    /** True if the drawable's resources have been recycled. */
    private boolean isRecycled;
    /**
     * True if the drawable is currently visible. Default to true because on certain platforms (at
     * least 4.1.1), setVisible is not called on {@link Drawable Drawables}
     * during {@link android.widget.ImageView#setImageDrawable(Drawable)}.
     * See issue #130.
     */
    private boolean isVisible = true;
    /** The number of times we've looped over all the frames in the animation. */
    private int loopCount;
    /** The number of times to loop through the animation. */
    private int maxLoopCount = LOOP_FOREVER;

    private boolean applyGravity;
    private Paint paint;
    private Rect destRect;

    /** Callbacks to notify loop completion of the animation, where the loop count is explicitly specified. */
    private List<WeakReference<AnimationCallback>> animationCallbacks;


    @SuppressWarnings("deprecation")
    @Deprecated
    public ApngDrawable(
            Context context,
            ApngDecoder frameDecoder,
            @SuppressWarnings("unused") BitmapPool bitmapPool,
            Transformation<ApngFrame> frameTransformation,
            int targetFrameWidth,
            int targetFrameHeight,
            ApngFrame firstFrame) {
        this(context, frameDecoder, frameTransformation, targetFrameWidth, targetFrameHeight, firstFrame);
    }

    public ApngDrawable(
            Context context,
            ApngDecoder frameDecoder,
            Transformation<ApngFrame> frameTransformation,
            int targetFrameWidth,
            int targetFrameHeight,
            ApngFrame firstFrame) {
        this(new ApngDrawable.FrameState(
                new ApngLoader(
                        // TODO(b/27524013): Factor out this call to Glide.get()
                        Glide.get(context),
                        frameDecoder,
                        targetFrameWidth,
                        targetFrameHeight,
                        frameTransformation,
                        firstFrame)));
        destRect = new Rect(0, 0, frameDecoder.getWidth(), frameDecoder.getHeight());
    }

    ApngDrawable(ApngDrawable.FrameState state) {
        this.state = Preconditions.checkNotNull(state);
        setLoopCount(LOOP_INTRINSIC);
    }

    public int getSize() {
        return state.frameLoader.getSize();
    }

    public ApngFrame getFirstFrame() {
        return state.frameLoader.getFirstFrame();
    }

    // Public API.
    @SuppressWarnings("WeakerAccess")
    public void setFrameTransformation(
            Transformation<ApngFrame> frameTransformation, ApngFrame firstFrame) {
        state.frameLoader.setFrameTransformation(frameTransformation, firstFrame);
    }

    public Transformation<ApngFrame> getFrameTransformation() {
        return state.frameLoader.getFrameTransformation();
    }

//    public ByteBuffer getBuffer() {
//        return state.frameLoader.getBuffer();
//    }

    public int getFrameCount() {
        return state.frameLoader.getFrameCount();
    }

    /**
     * Returns the current frame index in the range 0..{@link #getFrameCount()} - 1, or -1 if no frame
     * is displayed.
     */
    // Public API.
    @SuppressWarnings("WeakerAccess")
    public int getFrameIndex() {
        return state.frameLoader.getCurrentIndex();
    }

    private void resetLoopCount() {
        loopCount = 0;
    }

    /**
     * Starts the animation from the first frame. Can only be called while animation is not running.
     */
    // Public API.
    @SuppressWarnings("unused")
    public void startFromFirstFrame() {
        Preconditions.checkArgument(!isRunning, "You cannot restart a currently running animation.");
        state.frameLoader.setNextStartFromFirstFrame();
        start();
        notifyAnimationStartToListeners();
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
        Preconditions.checkArgument(
                !isRecycled,
                "You cannot start a recycled Drawable. Ensure that"
                        + "you clear any references to the Drawable when clearing the corresponding request.");
        // If we have only a single frame, we don't want to decode it endlessly.
        if (state.frameLoader.getFrameCount() == 1) {
            invalidateSelf();
        } else if (!isRunning) {
            isRunning = true;
            state.frameLoader.subscribe(this);
            invalidateSelf();
        }
    }

    private void stopRunning() {
        isRunning = false;
        state.frameLoader.unsubscribe(this);
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        Preconditions.checkArgument(
                !isRecycled,
                "Cannot change the visibility of a recycled resource."
                        + " Ensure that you unset the Drawable from your View before changing the View's"
                        + " visibility.");
        isVisible = visible;
        if (!visible) {
            stopRunning();
        } else if (isStarted) {
            startRunning();
        }
        return super.setVisible(visible, restart);
    }

    @Override
    public int getIntrinsicWidth() {
        return state.frameLoader.getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return state.frameLoader.getHeight();
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    // For testing.
    void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        applyGravity = true;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (isRecycled) {
            return;
        }
        if (applyGravity) {
            Gravity.apply(GRAVITY, getIntrinsicWidth(), getIntrinsicHeight(), getBounds(), getDestRect());
            applyGravity = false;
        }
        ApngFrame currentFrame = state.frameLoader.getCurrentFrame();
        if (currentFrame != null && !currentFrame.isRecycled()) {
            currentFrame.draw(canvas, destRect, getBounds(), getPaint());
        }
    }

    @Override
    public void setAlpha(int i) {
        getPaint().setAlpha(i);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        getPaint().setColorFilter(colorFilter);
    }

    private Rect getDestRect() {
        if (destRect == null) {
            destRect = new Rect();
        }
        return destRect;
    }

    private Paint getPaint() {
        if (paint == null) {
            paint = new Paint(Paint.FILTER_BITMAP_FLAG);
            paint.setAntiAlias(true);
        }
        return paint;
    }

    @Override
    public int getOpacity() {
        // We can't tell, so default to transparent to be safe.
        return PixelFormat.TRANSPARENT;
    }

    // See #1087.
    private Drawable.Callback findCallback() {
        Callback callback = getCallback();
        while (callback instanceof Drawable) {
            callback = ((Drawable) callback).getCallback();
        }
        return callback;
    }

    @Override
    public void onFrameReady() {
        if (findCallback() == null) {
            stop();
            invalidateSelf();
            return;
        }
        invalidateSelf();
        if (getFrameIndex() == getFrameCount() - 1) {
            loopCount++;
        }
        if (maxLoopCount != LOOP_FOREVER && loopCount >= maxLoopCount) {
            notifyAnimationEndToListeners();
            stop();
        }
    }

    private void notifyAnimationStartToListeners() {
        if (animationCallbacks != null && animationCallbacks.size() > 0) {
            for (WeakReference<AnimationCallback> weakReference : animationCallbacks) {
                if (weakReference != null) {
                    AnimationCallback animationCallback = weakReference.get();
                    if (animationCallback != null) {
                        animationCallback.onAnimationStart(this);
                    }
                }
            }
        }
    }

    private void notifyAnimationEndToListeners() {
        if (animationCallbacks != null && animationCallbacks.size() > 0) {
            for (WeakReference<AnimationCallback> weakReference : animationCallbacks) {
                if (weakReference != null) {
                    AnimationCallback animationCallback = weakReference.get();
                    if (animationCallback != null) {
                        animationCallback.onAnimationEnd(this);
                    }
                }
            }
        }
    }

    @Override
    public ConstantState getConstantState() {
        return state;
    }

    /** Clears any resources for loading frames that are currently held on to by this object. */
    public void recycle() {
        isRecycled = true;
        state.frameLoader.clear();
    }

    // For testing.
    boolean isRecycled() {
        return isRecycled;
    }

    // Public API.
    @SuppressWarnings("WeakerAccess")
    public void setLoopCount(int loopCount) {
        if (loopCount <= 0 && loopCount != LOOP_FOREVER && loopCount != LOOP_INTRINSIC) {
            throw new IllegalArgumentException(
                    "Loop count must be greater than 0, or equal to "
                            + "GlideDrawable.LOOP_FOREVER, or equal to GlideDrawable.LOOP_INTRINSIC");
        }

        if (loopCount == LOOP_INTRINSIC) {
            int intrinsicCount = state.frameLoader.getLoopCount();
            maxLoopCount =
                    (intrinsicCount == TOTAL_ITERATION_COUNT_FOREVER) ? LOOP_FOREVER : intrinsicCount;
        } else {
            maxLoopCount = loopCount;
        }
    }

    /**
     * Register callback to listen to AnimFrameDrawable animation end event after specific loop count set by
     *
     * <p>Note: This will only be called if the Gif stop because it reaches the loop count. Unregister
     * this in onLoadCleared to avoid potential memory leak.
     *
     * @param animationCallback Animation callback {@link AnimationCallback}.
     */
    @Override
    public void registerAnimationCallback(@NonNull AnimationCallback animationCallback) {
        if (animationCallback == null) {
            return;
        }
        if (animationCallbacks == null) {
            animationCallbacks = new ArrayList<>();
        }
        animationCallbacks.add(new WeakReference<AnimationCallback>(animationCallback));
    }

    @Override
    public boolean unregisterAnimationCallback(@NonNull AnimationCallback animationCallback) {
        if (animationCallbacks == null || animationCallback == null) {
            return false;
        }
        for (Iterator<WeakReference<AnimationCallback>> iterable = animationCallbacks.iterator(); iterable.hasNext();) {
            WeakReference<AnimationCallback> weakReference = iterable.next();
            if (weakReference == null) {
                iterable.remove();
                return true;
            } else if (weakReference.get() == animationCallback) {
                weakReference.clear();
                iterable.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public void clearAnimationCallbacks() {
        if (animationCallbacks != null) {
            animationCallbacks.clear();
        }
    }

    static final class FrameState extends ConstantState {
        @VisibleForTesting
        final ApngLoader frameLoader;

        FrameState(ApngLoader frameLoader) {
            this.frameLoader = frameLoader;
        }

        @NonNull
        @Override
        public Drawable newDrawable(Resources res) {
            return newDrawable();
        }

        @NonNull
        @Override
        public Drawable newDrawable() {
            return new ApngDrawable(this);
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }
    }
}


