package cn.yue.base.frame.apng2;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.github.penfeizhou.animation.decode.FrameSeqDecoder;

public class ApngWrapperFactory implements ModelLoaderFactory<Integer, FrameSeqDecoder> {

    private final Context context;
    public ApngWrapperFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ModelLoader<Integer, FrameSeqDecoder> build(
        @NonNull MultiModelLoaderFactory multiFactory) {
      return new ApngModelLoader(context);
    }

    @Override
    public void teardown() {
      // Do nothing.
    }
  }