package cn.yue.base.frame.anim;

import androidx.annotation.Nullable;

public interface IFrameDecoder {

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

    int getWidth();

    int getHeight();
    
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
     * @return Frame representation of frame.
     */
    @Nullable
    Frame getNextFrame();

    void clear();

}

