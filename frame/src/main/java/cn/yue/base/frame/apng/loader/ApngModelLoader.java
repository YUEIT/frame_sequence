package cn.yue.base.frame.apng.loader;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;

import cn.yue.base.frame.apng.io.APNGReader;
import cn.yue.base.frame.apng.io.StreamReader;

/**
 * Description :
 * Created by yue on 2021/12/8
 */

public class ApngModelLoader implements ModelLoader<Integer, ApngWrapper> {

    private final Context context;

    public ApngModelLoader(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public LoadData<ApngWrapper> buildLoadData(@NonNull Integer resourceId, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(resourceId), new ApngModelLoader.AngDataFetcher(context, resourceId));
    }

    @Override
    public boolean handles(@NonNull Integer integer) {
        return true;
    }


    private static class AngDataFetcher implements DataFetcher<ApngWrapper> {

        private final Context context;
        private final int resourceId;
        AngDataFetcher(Context context, int resourceId) {
            this.context = context;
            this.resourceId = resourceId;
        }

        @Override
        public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super ApngWrapper> callback) {
            try {
                InputStream inputStream = context.getResources().openRawResource(resourceId);
                StreamReader streamReader = new StreamReader(inputStream);
                APNGReader reader = new APNGReader(streamReader);
                ApngWrapper wrapper = ApngParser.parse(reader);
                wrapper.setResourceId(resourceId);
                callback.onDataReady(wrapper);
            } catch (Exception e) {
                callback.onLoadFailed(e);
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
        public Class<ApngWrapper> getDataClass() {
            return ApngWrapper.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }
}
