package cn.yue.base.frame.apng2;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;
import com.github.penfeizhou.animation.apng.decode.APNGDecoder;
import com.github.penfeizhou.animation.apng.decode.APNGParser;
import com.github.penfeizhou.animation.decode.FrameSeqDecoder;
import com.github.penfeizhou.animation.io.StreamReader;
import com.github.penfeizhou.animation.loader.Loader;
import com.github.penfeizhou.animation.loader.ResourceStreamLoader;

/**
 * Description :
 * Created by yue on 2021/12/8
 */

public class ApngModelLoader implements ModelLoader<Integer, FrameSeqDecoder> {

    private final Context context;

    public ApngModelLoader(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public LoadData<FrameSeqDecoder> buildLoadData(@NonNull Integer resourceId, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(resourceId), new AngDataFetcher(context, resourceId));
    }

    @Override
    public boolean handles(@NonNull Integer integer) {
        try {
            return APNGParser.isAPNG(new StreamReader(context.getResources().openRawResource(integer)));
        } catch (Exception e) {
            return false;
        }
    }


    private static class AngDataFetcher implements DataFetcher<FrameSeqDecoder> {

        private final Context context;
        private final int resourceId;
        AngDataFetcher(Context context, int resourceId) {
            this.context = context;
            this.resourceId = resourceId;
        }

        @Override
        public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super FrameSeqDecoder> callback) {
            try {
                Loader loader = new ResourceStreamLoader(context, resourceId);
                callback.onDataReady(new APNGDecoder(loader, null));
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
        public Class<FrameSeqDecoder> getDataClass() {
            return FrameSeqDecoder.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }
}
