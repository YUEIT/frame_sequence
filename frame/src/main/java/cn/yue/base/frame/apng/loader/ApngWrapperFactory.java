package cn.yue.base.frame.apng.loader;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

public class ApngWrapperFactory implements ModelLoaderFactory<Integer, ApngWrapper> {

    private final Context context;
    public ApngWrapperFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ModelLoader<Integer, ApngWrapper> build(
        @NonNull MultiModelLoaderFactory multiFactory) {
      return new ApngModelLoader(context);
    }

    @Override
    public void teardown() {
      // Do nothing.
    }
  }