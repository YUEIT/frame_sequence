package cn.yue.base.frame.apng.loader;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

/**
 * Description :
 * Created by yue on 2021/12/3
 */

public class ApngFrame {

    IFrame iFrame;

    public void setSource(ApngWrapper.ApngFrameResource frameResource,
                          Bitmap bitmap, ApngRender apngRender) {
        if (bitmap == null) {
            return;
        }
        iFrame = new FrameBitmap(frameResource, bitmap, apngRender);
    }

    public IFrame getFrameSource() {
        return iFrame;
    }

    public int getSize() {
        if (iFrame == null) {
            return 0;
        }
        return iFrame.getSize();
    }

    public void initialize() {
        if (iFrame == null) {
            return;
        }
        iFrame.initialize();
    }

    public int getWidth() {
        if (iFrame == null) {
            return 0;
        }
        return iFrame.getWidth();
    }

    public int getHeight() {
        if (iFrame == null) {
            return 0;
        }
        return iFrame.getHeight();
    }

    public void recycle() {
        if (iFrame == null) {
            return;
        }
        iFrame.recycle();
    }

    public boolean isRecycled() {
        if (iFrame == null) {
            return true;
        }
        return iFrame.isRecycled();
    }

    public void draw(Canvas canvas, Rect dest, Rect bounds, Paint paint) {
        if (iFrame == null) {
            return;
        }
        iFrame.draw(canvas, dest, bounds, paint);
    }

    public static class FrameBitmap implements IFrame {

        private final ApngWrapper.ApngFrameResource frameResource;
        private final Bitmap bitmap;
        private ApngRender apngRender;
        public FrameBitmap(ApngWrapper.ApngFrameResource frameResource,
                         Bitmap bitmap, ApngRender apngRender) {
            this.frameResource = frameResource;
            this.bitmap = bitmap;
            this.apngRender = apngRender;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        @Override
        public void initialize() {

        }

        @Override
        public int getWidth() {
            return bitmap.getWidth();
        }

        @Override
        public int getHeight() {
            return bitmap.getHeight();
        }

        @Override
        public int getSize() {
            return bitmap.getAllocationByteCount();
        }

        @Override
        public void recycle() {
            bitmap.recycle();
        }

        @Override
        public boolean isRecycled() {
            return bitmap.isRecycled();
        }

        @Override
        public void draw(Canvas canvas, Rect dest, Rect bounds, Paint paint) {
//            bounds.offsetTo(frameResource.frameX, frameResource.frameY);
//            bounds.set(frameResource.frameX, frameResource.frameY ,
//                    frameResource.frameX + frameResource.frameWidth,
//                    frameResource.frameY + frameResource.frameHeight);
//            canvas.drawBitmap(bitmap, null, bounds, paint);
            apngRender.renderDraw(canvas, bounds, paint, frameResource, bitmap);
        }
    }

    public interface IFrame {
        void initialize();
        int getWidth();
        int getHeight();
        int getSize();
        void recycle();
        boolean isRecycled();
        void draw(Canvas canvas, Rect dest, Rect bounds, Paint paint);
    }


}
