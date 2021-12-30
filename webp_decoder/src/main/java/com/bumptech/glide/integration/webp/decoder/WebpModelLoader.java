package com.bumptech.glide.integration.webp.decoder;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Description :
 * Created by yue on 2021/11/29
 */

public class WebpModelLoader implements ModelLoader<Integer, WrapInputStream> {

    private final Resources resources;
    public WebpModelLoader(Resources resources) {
        this.resources = resources;
    }

    @Nullable
    @Override
    public LoadData<WrapInputStream> buildLoadData(@NonNull Integer integer, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(integer), new AnimDataFetcher(resources, integer));
    }

    @Override
    public boolean handles(@NonNull Integer integer) {
        return true;
    }

    public static class AnimDataFetcher implements DataFetcher<WrapInputStream> {

        private final Resources resources;
        private final int resourceId;
        AnimDataFetcher(Resources resources, int resourceId) {
           this.resources = resources;
           this.resourceId = resourceId;
        }

        @Override
        public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super WrapInputStream> callback) {
            try {
                InputStream stream = resources.openRawResource(resourceId);
                WrapInputStream wrapInputStream = new WrapInputStream();
                wrapInputStream.setInputStream(stream);
                callback.onDataReady(wrapInputStream);
            } catch (Exception e) {
                callback.onLoadFailed(e);
                e.printStackTrace();
            }
        }

        @Override
        public void cleanup() {

        }

        @Override
        public void cancel() {

        }

        @NonNull
        @Override
        public Class<WrapInputStream> getDataClass() {
            return WrapInputStream.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }
}
