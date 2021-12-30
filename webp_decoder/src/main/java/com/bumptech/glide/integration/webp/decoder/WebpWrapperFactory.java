package com.bumptech.glide.integration.webp.decoder;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

public class WebpWrapperFactory implements ModelLoaderFactory<Integer, WrapInputStream> {

    private final Context context;
    public WebpWrapperFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ModelLoader<Integer, WrapInputStream> build(
        @NonNull MultiModelLoaderFactory multiFactory) {
      return new WebpModelLoader(context.getResources());
    }

    @Override
    public void teardown() {
      // Do nothing.
    }
  }