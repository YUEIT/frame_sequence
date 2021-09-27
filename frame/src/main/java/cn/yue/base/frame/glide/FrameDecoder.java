package cn.yue.base.frame.glide;

import android.graphics.Bitmap;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.gifdecoder.GifDecoder;
import com.bumptech.glide.gifdecoder.GifHeader;

import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;

public interface FrameDecoder {

    /**
     * File read status: No errors.
     */
    int STATUS_OK = 0;
    /**
     * File read status: Error decoding file (may be partially decoded).
     */
    int STATUS_FORMAT_ERROR = 1;
    /**
     * File read status: Unable to open source.
     */
    int STATUS_OPEN_ERROR = 2;
    /**
     * Unable to fully decode the current frame.
     */
    int STATUS_PARTIAL_DECODE = 3;
    /**
     * The total iteration count which means repeat forever.
     */
    int TOTAL_ITERATION_COUNT_FOREVER = 0;

    /**
     * An interface that can be used to provide reused {@link android.graphics.Bitmap}s to avoid GCs
     * from constantly allocating {@link android.graphics.Bitmap}s for every frame.
     */
    interface BitmapProvider {
        /**
         * Returns an {@link Bitmap} with exactly the given dimensions and config.
         *
         * @param width  The width in pixels of the desired {@link android.graphics.Bitmap}.
         * @param height The height in pixels of the desired {@link android.graphics.Bitmap}.
         * @param config The {@link android.graphics.Bitmap.Config} of the desired {@link
         *               android.graphics.Bitmap}.
         */
        @NonNull
        Bitmap obtain(int width, int height, @NonNull Bitmap.Config config);

        /**
         * Releases the given Bitmap back to the pool.
         */
        void release(@NonNull Bitmap bitmap);

        /**
         * Returns a byte array used for decoding and generating the frame bitmap.
         *
         * @param size the size of the byte array to obtain
         */
        @NonNull
        byte[] obtainByteArray(int size);

        /**
         * Releases the given byte array back to the pool.
         */
        void release(@NonNull byte[] bytes);

        /**
         * Returns an int array used for decoding/generating the frame bitmaps.
         */
        @NonNull
        int[] obtainIntArray(int size);

        /**
         * Release the given array back to the pool.
         */
        void release(@NonNull int[] array);
    }

    int getWidth();

    int getHeight();

    @NonNull
    ByteBuffer getData();

    /**
     * Move the animation frame counter forward.
     */
    void advance();

    /**
     * Gets display duration for specified frame.
     *
     * @param n int index of frame.
     * @return delay in milliseconds.
     */
    int getDelay(int n);

    /**
     * Gets display duration for the upcoming frame in ms.
     */
    int getNextDelay();

    /**
     * Gets the number of frames read from file.
     *
     * @return frame count.
     */
    int getFrameCount();

    /**
     * Gets the current index of the animation frame, or -1 if animation hasn't not yet started.
     *
     * @return frame index.
     */
    int getCurrentFrameIndex();

    /**
     * Resets the frame pointer to before the 0th frame, as if we'd never used this decoder to
     * decode any frames.
     */
    void resetFrameIndex();

    /**
     * Gets the "Netscape" loop count, if any. A count of 0 means repeat indefinitely.
     *
     * @return loop count if one was specified, else 1.
     * This method cannot distinguish whether the loop count is 1 or doesn't exist.
     */
    int getLoopCount();

    int getByteSize();

    /**
     * Get the next frame in the animation sequence.
     *
     * @return Bitmap representation of frame.
     */
    @Nullable
    Bitmap getNextFrame();

    void clear();

}

