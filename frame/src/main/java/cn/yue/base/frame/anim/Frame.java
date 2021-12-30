package cn.yue.base.frame.anim;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Description :
 * Created by yue on 2021/12/3
 */

public class Frame {

    IFrame iFrame;

    public void setSource(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        iFrame = new FrameBitmap(bitmap);
    }

    public void setSource(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        iFrame = new FrameDrawable(drawable);
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

        public FrameBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        private final Bitmap bitmap;

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
            canvas.drawBitmap(bitmap, null, bounds, paint);
        }
    }

    public static class FrameDrawable implements IFrame {

        public FrameDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

        private Drawable drawable;

        public Drawable getDrawable() {
            return drawable;
        }

        @Override
        public void initialize() {

        }

        @Override
        public int getWidth() {
            return drawable.getIntrinsicWidth();
        }

        @Override
        public int getHeight() {
            return drawable.getIntrinsicHeight();
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void recycle() {
            drawable = null;
        }

        @Override
        public boolean isRecycled() {
            return drawable == null;
        }


        @Override
        public void draw(Canvas canvas, Rect dest, Rect bounds, Paint paint) {
            drawable.setBounds(bounds);
            drawable.draw(canvas);
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
