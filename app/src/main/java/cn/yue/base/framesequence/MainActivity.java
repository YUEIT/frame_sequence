package cn.yue.base.framesequence;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.penfeizhou.animation.apng.APNGDrawable;

import cn.yue.base.frame.GlideApp;
import cn.yue.base.frame.anim.LoopTarget;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageView = findViewById(R.id.image);
        loadImage(imageView);
    }


    private void loadImage(final ImageView imageView) {
//        imageView.setImageResource(R.drawable.anim_dog);
//        AnimationDrawable mAnimationDrawable = (AnimationDrawable) imageView.getDrawable();
//        mAnimationDrawable.setOneShot(false);
//        mAnimationDrawable.start();

//        GlideApp.with(MainActivity.this)
//            .asAnim()
//            .load(R.drawable.anim_dog)
//                .into(imageView);
//        GlideApp.with(this)
//                .load(R.drawable.anim)
//                .into(imageView);

//        Glide.with(this)
//                .load(R.drawable.anim_dog)
//                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE).skipMemoryCache(true))
//                .into(new LoopTarget<>(imageView, 1, new Animatable2Compat.AnimationCallback() {
//                    @Override
//                    public void onAnimationEnd(Drawable drawable) {
//                        super.onAnimationEnd(drawable);
//                        imageView.setVisibility(View.GONE);
//                    }
//                }));

//        GlideApp.with(this)
//                .asApng()
//                .load(R.drawable.iiisss)
//                .into(imageView);

        GlideApp.with(this)
                .asApng()
                .load("https://misc.aotu.io/ONE-SUNDAY/SteamEngine.png")
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE).skipMemoryCache(true))
                .into(new LoopTarget<APNGDrawable>(imageView, 1, new Animatable2Compat.AnimationCallback() {
                    @Override
                    public void onAnimationStart(Drawable drawable) {
                        super.onAnimationStart(drawable);
                    }

                    @Override
                    public void onAnimationEnd(Drawable drawable) {
                        super.onAnimationEnd(drawable);
                        Log.d("luo", "onAnimationEnd: ");
                    }
                }));
    }
}