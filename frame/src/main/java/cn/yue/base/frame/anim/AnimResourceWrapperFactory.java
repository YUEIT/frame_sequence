package cn.yue.base.frame.anim;

import android.content.Context;
import android.content.res.Resources;

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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnimResourceWrapperFactory implements ModelLoaderFactory<Integer, AnimWrapper> {

    private final Context context;
    public AnimResourceWrapperFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ModelLoader<Integer, AnimWrapper> build(
        @NonNull MultiModelLoaderFactory multiFactory) {
      return new AnimFrameModelLoader(context.getResources());
    }

    @Override
    public void teardown() {
      // Do nothing.
    }

    public static class AnimFrameModelLoader implements ModelLoader<Integer, AnimWrapper> {

        private final Resources resources;
        public AnimFrameModelLoader(Resources resources) {
            this.resources = resources;
        }

        @Nullable
        @Override
        public LoadData<AnimWrapper> buildLoadData(@NonNull Integer integer, int width, int height, @NonNull Options options) {
            return new LoadData<>(new ObjectKey(integer), new AnimDataFetcher(resources, integer));
        }

        @Override
        public boolean handles(@NonNull Integer integer) {
            try {
                return resources.getAnimation(integer) != null;
            } catch (Resources.NotFoundException e) {
                return false;
            }
        }

        public static class AnimDataFetcher implements DataFetcher<AnimWrapper> {

            private final Resources resources;
            private final int resourceId;
            AnimDataFetcher(Resources resources, int resourceId) {
                this.resources = resources;
                this.resourceId = resourceId;
            }

            @Override
            public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super AnimWrapper> callback) {
                try {
                    List<AnimWrapper.FrameResource> list = parseXml(resources, resourceId);
                    callback.onDataReady(new AnimWrapper(list));
                } catch (Exception e) {
                    callback.onLoadFailed(e);
                    e.printStackTrace();
                }
            }

            private List<AnimWrapper.FrameResource> parseXml(Resources resources, int resourceId) throws XmlPullParserException, IOException {
                List<AnimWrapper.FrameResource> list = new ArrayList<>();
                XmlPullParser xmlParser = resources.getAnimation(resourceId);
                int eventType = xmlParser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xmlParser.getName().equals("item")) {
                            AnimWrapper.FrameResource animFrame = new AnimWrapper.FrameResource();
                            for (int i = 0; i < xmlParser.getAttributeCount(); i++) {
                                String key = xmlParser.getAttributeName(i);
                                String value = xmlParser.getAttributeValue(i);
                                if (key.equals("drawable")) {
                                    animFrame.setResource(value);
                                } else if (key.equals("duration")) {
                                    animFrame.setDuration(Long.parseLong(value));
                                }
                            }
                            list.add(animFrame);
                        }
                    }
                    eventType = xmlParser.next();
                }

                return list;
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