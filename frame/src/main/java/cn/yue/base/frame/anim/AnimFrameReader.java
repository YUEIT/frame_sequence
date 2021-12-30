package cn.yue.base.frame.anim;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.DisplayMetrics;

import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Description :
 * Created by yue on 2021/11/11
 */

public class AnimFrameReader {

    private final Bitmap.Config config;
    private final AnimWrapper animWrapper;
    private final Context context;
    private int width;
    private int height;
    private final Map<Integer, Frame> cacheFrame = new HashMap<>();
    private final boolean requireCache;

    public AnimFrameReader(Context context, AnimWrapper animWrapper, Bitmap.Config config, boolean requireCache) {
        this.animWrapper = animWrapper;
        this.context = context;
        this.config = config;
        this.requireCache = requireCache;
    }

    private AnimWrapper.FrameResource getAnimFrame(int position) {
        if (animWrapper.getResourceList().size() < position) {
            return null;
        }
        return animWrapper.getResourceList().get(position);
    }

    public void getFrame(int frameNr, final AnimFrameDecoder.PackFrame output) {
        AnimWrapper.FrameResource frameResource = getAnimFrame(frameNr);
        if (frameResource == null) {
            return;
        }
        if (frameResource.getSourceType() == AnimWrapper.FrameResource.TYPE_LOCAL) {
            getFrameByLocal(frameNr, output, frameResource.getResourceId());
        } else if (frameResource.getSourceType() == AnimWrapper.FrameResource.TYPE_DISK) {
            getFrameByDisk(frameNr, output, frameResource.getResource());
        }
    }

    private void getFrameByLocal(int frameNr, final AnimFrameDecoder.PackFrame output, int resourceId) {
        if (cacheFrame.size() > 0) {
            Frame frame = cacheFrame.get(frameNr);
            if (frame != null) {
                output.frame = frame;
                return;
            }
        }
        try {
            final InputStream is = context.getResources().openRawResource(resourceId);
            if (FileType.isImageFile(is)) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;
                options.inSampleSize = 1;
                options.inPreferredConfig = config;
                if (requireCache) {
                    Bitmap newBitmap = BitmapFactory.decodeStream(is, null, options);
                    computeBitmapSize(newBitmap);
                    Frame frame = new Frame();
                    frame.setSource(newBitmap);
                    output.frame = frame;
                    cacheFrame.put(frameNr, frame);
                } else {
                    if (output.frame == null) {
                        output.frame = new Frame();
                    } else if (!output.frame.isRecycled() && output.frame.getFrameSource() instanceof Frame.FrameBitmap) {
                        options.inBitmap = ((Frame.FrameBitmap) output.frame.getFrameSource()).getBitmap();
                    }
                    Bitmap newBitmap = BitmapFactory.decodeStream(is, null, options);
                    computeBitmapSize(newBitmap);
                    output.frame.setSource(newBitmap);
                }
            } else {
                Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), resourceId, null);
                computeBitmapSize(drawable);
                if (requireCache) {
                    Frame frame = new Frame();
                    frame.setSource(drawable);
                    output.frame = frame;
                    cacheFrame.put(frameNr, frame);
                } else {
                    if (output.frame == null) {
                        output.frame = new Frame();
                    }
                    output.frame.setSource(drawable);
                }
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getFrameByDisk(int frameNr, final AnimFrameDecoder.PackFrame output, String resource) {
        if (cacheFrame.size() > 0) {
            Frame frame = cacheFrame.get(frameNr);
            if (frame != null) {
                output.frame = frame;
                return;
            }
        }
        try {
            File file = new File(resource);
            final InputStream is = new FileInputStream(file);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            options.inSampleSize = 1;
            options.inPreferredConfig = config;
            if (requireCache) {
                Bitmap newBitmap = BitmapFactory.decodeStream(is, null, options);
                computeBitmapSize(newBitmap);
                Frame frame = new Frame();
                frame.setSource(newBitmap);
                output.frame = frame;
                cacheFrame.put(frameNr, frame);
            } else {
                if (output.frame == null) {
                    output.frame = new Frame();
                } else if (!output.frame.isRecycled() && output.frame.getFrameSource() instanceof Frame.FrameBitmap) {
                    options.inBitmap = ((Frame.FrameBitmap) output.frame.getFrameSource()).getBitmap();
                }
                Bitmap newBitmap = BitmapFactory.decodeStream(is, null, options);
                computeBitmapSize(newBitmap);
                output.frame.setSource(newBitmap);
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Uri getResourceUri(Integer model) {
        try {
            return Uri.parse(
                    ContentResolver.SCHEME_ANDROID_RESOURCE
                            + "://"
                            + context.getResources().getResourcePackageName(model)
                            + '/'
                            + context.getResources().getResourceTypeName(model)
                            + '/'
                            + context.getResources().getResourceEntryName(model));
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }

    public int getFrameCount() {
        return animWrapper.getResourceList().size();
    }

    public long getDelay(int framePointer) {
        if (framePointer < 0) {
            return 0;
        }
        AnimWrapper.FrameResource frameResource = getAnimFrame(framePointer);
        if (frameResource == null) {
            return 0;
        }
        return frameResource.getDuration();
    }

    private void computeBitmapSize(Bitmap bitmap) {
        int mTargetDensity = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mTargetDensity = DisplayMetrics.DENSITY_DEVICE_STABLE;
        } else {
            mTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
        }
        if (bitmap != null) {
            width = bitmap.getScaledWidth(mTargetDensity);
            height = bitmap.getScaledHeight(mTargetDensity);
        } else {
            width = height = -1;
        }
    }

    private void computeBitmapSize(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        width = drawable.getIntrinsicWidth();
        height = drawable.getIntrinsicHeight();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void release() {
        for (Map.Entry<Integer, Frame> entry : cacheFrame.entrySet()) {
            Frame frame = entry.getValue();
            frame.recycle();
        }
        cacheFrame.clear();
    }
}

