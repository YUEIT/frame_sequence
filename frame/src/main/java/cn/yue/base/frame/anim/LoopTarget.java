package cn.yue.base.frame.anim;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

import com.bumptech.glide.request.target.ImageViewTarget;
import com.github.penfeizhou.animation.apng.APNGDrawable;

/**
 * Description :
 * Created by yue on 2021/12/6
 */

public class LoopTarget<T extends Drawable> extends ImageViewTarget<T> {

    public static final int LOOP_FOREVER = -1;
    /**
     * 循环次数，0，-1 无限循环
     */
    private final int loopCount;
    /**
     * 动画监听
     */
    private Animatable2Compat.AnimationCallback animationCallback;
    private AnimFrameDrawable animFrameDrawable;
    private APNGDrawable apngDrawable;

    public LoopTarget(ImageView view) {
        this(view, 1);
    }

    public LoopTarget(ImageView view, int loopCount) {
        this(view, loopCount, null);
    }

    public LoopTarget(ImageView view, Animatable2Compat.AnimationCallback animationCallback) {
        this(view, 1, animationCallback);
    }

    public LoopTarget(ImageView view, int loopCount, Animatable2Compat.AnimationCallback animationCallback) {
        super(view);
        this.loopCount = loopCount;
        this.animationCallback = animationCallback;
    }

    /**
     * true: 退入后台时依旧迭代帧数，不会暂停; 但是不会绘制，返回前台时继续绘制
     */
    protected boolean enableBackgroundRunning() {
        return false;
    }

    @Override
    protected void setResource(@Nullable Drawable resource) {
        if (resource instanceof AnimFrameDrawable) {
            animFrameDrawable = (AnimFrameDrawable)resource;
            animFrameDrawable.setLoopCount(loopCount);
            animFrameDrawable.enableBackgroundRunning(enableBackgroundRunning());
            animFrameDrawable.startFromFirstFrame();
            if (animationCallback != null) {
                animFrameDrawable.registerAnimationCallback(animationCallback);
            }
            view.setImageDrawable(resource);
        } else if (resource instanceof APNGDrawable) {
            apngDrawable = (APNGDrawable)resource;
            apngDrawable.setLoopLimit(loopCount);
            apngDrawable.reset();
            if (animationCallback != null) {
                apngDrawable.registerAnimationCallback(animationCallback);
            }
            view.setImageDrawable(resource);
        }
    }

    @Override
    public void onLoadStarted(@Nullable Drawable placeholder) {
        super.onLoadStarted(placeholder);
    }

    @Override
    public void onLoadFailed(@Nullable Drawable errorDrawable) {
        super.onLoadFailed(errorDrawable);
    }

    @Override
    public void onLoadCleared(@Nullable Drawable placeholder) {
        super.onLoadCleared(placeholder);
        if (animFrameDrawable != null) {
            animFrameDrawable.unregisterAnimationCallback(animationCallback);
        }
        if (apngDrawable != null) {
            apngDrawable.unregisterAnimationCallback(animationCallback);
        }
    }

    @Override
    public void setDrawable(Drawable drawable) {
        if (drawable != null) {
            super.setDrawable(drawable);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (animFrameDrawable != null) {
            animFrameDrawable.onTargetDestroy();
        }
        animFrameDrawable = null;
        animationCallback = null;
        apngDrawable = null;
    }
}
