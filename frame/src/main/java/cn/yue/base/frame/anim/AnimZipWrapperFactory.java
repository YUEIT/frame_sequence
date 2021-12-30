package cn.yue.base.frame.anim;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AnimZipWrapperFactory implements ModelLoaderFactory<String, AnimWrapper> {

    private final Context context;
    public AnimZipWrapperFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ModelLoader<String, AnimWrapper> build(
        @NonNull MultiModelLoaderFactory multiFactory) {
      return new AnimZipModelLoader(context);
    }

    @Override
    public void teardown() {
      // Do nothing.
    }

    public static class AnimZipModelLoader implements ModelLoader<String, AnimWrapper> {

        private final Context context;
        public AnimZipModelLoader(Context context) {
            this.context = context;
        }

        @Nullable
        @Override
        public LoadData<AnimWrapper> buildLoadData(@NonNull String files, int width, int height, @NonNull Options options) {
            return new LoadData<>(new ObjectKey(files), new AnimDataFetcher(context, files));
        }

        @Override
        public boolean handles(@NonNull String files) {
            return files.endsWith("zip");
        }

        public static class AnimDataFetcher implements DataFetcher<AnimWrapper> {

            private final Context context;
            private final String files;
            AnimDataFetcher(Context context, String files) {
                this.context = context;
                this.files = files;
            }

            @Override
            public void loadData(@NonNull Priority priority, @NonNull final DataCallback<? super AnimWrapper> callback) {
                try {
                    if (files.startsWith("http")) {
                        Glide.with(context)
                                .asFile()
                                .load(files)
                                .into(new CustomTarget<File>() {

                                    @Override
                                    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                        AnimWrapper animWrapper = parseToWrapper(resource);
                                        if (animWrapper == null) {
                                            callback.onLoadFailed(new FileNotFoundException());
                                        } else {
                                            callback.onDataReady(animWrapper);
                                        }
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {

                                    }

                                    @Override
                                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                        super.onLoadFailed(errorDrawable);
                                        callback.onLoadFailed(new FileNotFoundException());
                                    }
                                });
                    } else {
                        AnimWrapper animWrapper = parseToWrapper(new File(files));
                        if (animWrapper == null) {
                            callback.onLoadFailed(new FileNotFoundException());
                        } else {
                            callback.onDataReady(animWrapper);
                        }
                    }
                } catch (Exception e) {
                    callback.onLoadFailed(e);
                    e.printStackTrace();
                }
            }

            private AnimWrapper parseToWrapper(File resource) {
                String folder = getFileNameByPath(files);
                //解压缩，如果存在则会立马返回文件夹
                String dataDir;
                if (resource.isDirectory()) {
                    dataDir = resource.getAbsolutePath();
                } else {
                    dataDir = unZip(resource.getAbsolutePath(), Glide.getPhotoCacheDir(context) + "/" + folder);
                }
                if (!TextUtils.isEmpty(dataDir)) {
                    File file = new File(dataDir);
                    if (file.exists() && file.isDirectory()) {
                        List<File> fileList = Arrays.asList(Objects.requireNonNull(file.listFiles(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                if (name.endsWith(".png") || name.endsWith(".jpg")) {
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        })));
                        Collections.sort(fileList, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                if (o1.isDirectory() && o2.isFile()) {
                                    return -1;
                                }
                                if (o1.isFile() && o2.isDirectory()) {
                                    return 1;
                                }
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                        List<AnimWrapper.FrameResource> list = new ArrayList<>();
                        for (File mFile : fileList.toArray(new File[0])) {
                            AnimWrapper.FrameResource frameResource = new AnimWrapper.FrameResource();
                            frameResource.setSourceType(AnimWrapper.FrameResource.TYPE_DISK);
                            frameResource.setResource(mFile.getAbsolutePath());
                            list.add(frameResource);
                        }
                        return new AnimWrapper(list);
                    }
                }
                return null;
            }

            public String getFileNameByPath(String path) {
                int start = path.lastIndexOf("/");
                int end = path.lastIndexOf(".");
                if (start != -1 && end != -1) {
                    return path.substring(start + 1, end);
                } else {
                    return null;
                }
            }

            public synchronized String unZip(String zipFile, String targetDir) {
                if (TextUtils.isEmpty(zipFile)) {
                    return null;
                } else {
                    File file = new File(zipFile);
                    if (!file.exists()) {
                        return null;
                    } else {
                        File targetFolder = new File(targetDir);
                        if (!targetFolder.exists()) {
                            targetFolder.mkdirs();
                        }

                        String dataDir = null;
                        short BUFFER = 4096;
                        FileInputStream fis = null;
                        ZipInputStream zis = null;
                        FileOutputStream fos = null;
                        BufferedOutputStream dest = null;

                        try {
                            fis = new FileInputStream(file);
                            zis = new ZipInputStream(new BufferedInputStream(fis));

                            while (true) {
                                String strEntry;
                                ZipEntry entry;
                                do {
                                    try {
                                        if ((entry = zis.getNextEntry()) == null) {
                                            return TextUtils.isEmpty(dataDir) ? targetDir : dataDir;
                                        }
                                    } catch (Exception e) {
                                        return null;
                                    }

                                    strEntry = entry.getName();
                                } while (strEntry.contains("../"));

                                if (entry.isDirectory()) {
                                    String count1 = targetDir + File.separator + strEntry;
                                    File data1 = new File(count1);
                                    if (!data1.exists()) {
                                        data1.mkdirs();
                                    }

                                    if (TextUtils.isEmpty(dataDir)) {
                                        dataDir = data1.getPath();
                                    }
                                } else {
                                    String targetFileDir = targetDir + File.separator + strEntry;
                                    File targetFile = new File(targetFileDir);
                                    if (!targetFile.exists()) {
                                        try {
                                            byte[] data = new byte[BUFFER];
                                            fos = new FileOutputStream(targetFile);
                                            dest = new BufferedOutputStream(fos, BUFFER);

                                            int count;
                                            while ((count = zis.read(data)) != -1) {
                                                dest.write(data, 0, count);
                                            }

                                            dest.flush();
                                        } catch (IOException var41) {
                                            ;
                                        } finally {
                                            try {
                                                if (dest != null) {
                                                    dest.close();
                                                }

                                                if (fos != null) {
                                                    fos.close();
                                                }
                                            } catch (IOException var40) {
                                                ;
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (IOException var43) {

                        } finally {
                            try {
                                if (zis != null) {
                                    zis.close();
                                }

                                if (fis != null) {
                                    fis.close();
                                }
                            } catch (IOException var39) {

                            }

                        }
                        return targetDir;
                    }
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