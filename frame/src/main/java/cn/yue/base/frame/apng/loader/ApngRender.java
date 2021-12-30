package cn.yue.base.frame.apng.loader;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import cn.yue.base.frame.R;
import cn.yue.base.frame.apng.chunk.FCTLChunk;

/**
 * Description :
 * Created by yue on 2021/12/8
 */

public class ApngRender {

    private Rect mDisposeRect = new Rect();
    private byte mLastDisposeOp = FCTLChunk.APNG_DISPOSE_OP_NON;

    private ApngWrapper.ApngFrameResource prFrame;
    private Bitmap prBitmap;

    public void renderDraw(Canvas canvas, Rect bounds, Paint paint,
                           ApngWrapper.ApngFrameResource frame, Bitmap bitmap) {
        ApngWrapper.ApngFrameResource showFrame = frame;
        Bitmap showBitmap = bitmap;
        switch (mLastDisposeOp) {
            case FCTLChunk.APNG_DISPOSE_OP_NON:
                // no op
                break;

            case FCTLChunk.APNG_DISPOSE_OP_BACKGROUND:
                // clear rect
                canvas.clipRect(mDisposeRect);
                canvas.drawColor(Color.TRANSPARENT);
                Rect currentRect = new Rect();
                int x = frame.frameX;
                int y = frame.frameY;
                currentRect.set(x, y, x + frame.frameWidth, y + frame.frameHeight);
                canvas.clipRect(currentRect);
                break;

            case FCTLChunk.APNG_DISPOSE_OP_PREVIOUS:
                // swap work and cache bitmap
                showFrame = prFrame;
                showBitmap = prBitmap;
                prBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                break;
        }

        // current frame dispose op
        mLastDisposeOp = frame.dispose_op;
        switch (mLastDisposeOp) {
            case FCTLChunk.APNG_DISPOSE_OP_NON:
                // no op
                break;

            case FCTLChunk.APNG_DISPOSE_OP_BACKGROUND:
                // cache rect for next clear dispose
                int x = frame.frameX;
                int y = frame.frameY;
                mDisposeRect.set(x, y, x + frame.frameWidth, y + frame.frameHeight);
                break;

            case FCTLChunk.APNG_DISPOSE_OP_PREVIOUS:
                // cache bmp for next restore dispose
                prFrame = frame;
                prBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                break;
        }
        if (frame.blend_op == FCTLChunk.APNG_BLEND_OP_SOURCE) {
            canvas.drawColor(Color.TRANSPARENT);
        }
        bounds.set(showFrame.frameX, showFrame.frameY ,
                showFrame.frameX + showFrame.frameWidth,
                showFrame.frameY + showFrame.frameHeight);
        canvas.drawBitmap(showBitmap, null, bounds, paint);
    }

}
