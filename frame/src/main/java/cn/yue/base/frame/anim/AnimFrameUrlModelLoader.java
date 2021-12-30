package cn.yue.base.frame.anim;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Description :
 * Created by yue on 2021/11/29
 */

public class AnimFrameUrlModelLoader implements ModelLoader<String, AnimWrapper> {

    private final Resources resources;
    public AnimFrameUrlModelLoader(Resources resources) {
        this.resources = resources;
    }

    @Nullable
    @Override
    public LoadData<AnimWrapper> buildLoadData(@NonNull String path, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(path), new AnimDataFetcher(resources, path));
    }

    @Override
    public boolean handles(@NonNull String integer) {
        return true;
    }

    public static class AnimDataFetcher implements DataFetcher<AnimWrapper> {

        private final Resources resources;
        private final String resourcePath;
        AnimDataFetcher(Resources resources, String resourcePath) {
           this.resources = resources;
           this.resourcePath = resourcePath;
        }

        /**
         * zip json file
         * @param priority
         * @param callback
         */

        @Override
        public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super AnimWrapper> callback) {
            try {
                List<AnimWrapper.FrameResource> list = parseFile(resources, resourcePath);
                callback.onDataReady(new AnimWrapper(list));
            } catch (Exception e) {
                callback.onLoadFailed(e);
                e.printStackTrace();
            }
        }

        private List<AnimWrapper.FrameResource> parseFile(Resources resources, String path) throws IOException {
            InputStream inputStream = resources.getAssets().open(path);
            ZipInputStream inZip = new ZipInputStream(inputStream);
            ZipEntry zipEntry;
            while ((zipEntry = inZip.getNextEntry()) != null) {
                zipEntry.getName();
            }
            inZip.close();
            return null;
        }

        @Override
        public void cleanup() {

        }

        @Override
        public void cancel() {

        }

        @NonNull
        @Override
        public Class<AnimWrapper> getDataClass() {
            return AnimWrapper.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }
}
