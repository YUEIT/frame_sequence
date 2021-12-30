package cn.yue.base.frame.anim;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AnimFileWrapperFactory implements ModelLoaderFactory<File[], AnimWrapper> {

    @NonNull
    @Override
    public ModelLoader<File[], AnimWrapper> build(
        @NonNull MultiModelLoaderFactory multiFactory) {
      return new AnimFileModelLoader();
    }

    @Override
    public void teardown() {
      // Do nothing.
    }

    public static class AnimFileModelLoader implements ModelLoader<File[], AnimWrapper> {

        @Nullable
        @Override
        public LoadData<AnimWrapper> buildLoadData(@NonNull File[] files, int width, int height, @NonNull Options options) {
            return new LoadData<>(new ObjectKey(files), new AnimDataFetcher(files));
        }

        @Override
        public boolean handles(@NonNull File[] files) {
            return true;
        }

        public static class AnimDataFetcher implements DataFetcher<AnimWrapper> {

            private final File[] files;
            AnimDataFetcher(File[] files) {
                this.files = files;
            }

            @Override
            public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super AnimWrapper> callback) {
                try {
                    List<AnimWrapper.FrameResource> list = new ArrayList<>();
                    for (File file : files) {
                        AnimWrapper.FrameResource frameResource = new AnimWrapper.FrameResource();
                        frameResource.setSourceType(AnimWrapper.FrameResource.TYPE_DISK);
                        frameResource.setResource(file.getAbsolutePath());
                        list.add(frameResource);
                    }
                    callback.onDataReady(new AnimWrapper(list));
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
}