package cn.yue.base.frame.apng.loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;

import cn.yue.base.frame.apng.chunk.Chunk;
import cn.yue.base.frame.apng.chunk.FCTLChunk;

/**
 * Description :
 * Created by yue on 2021/12/8
 */

public class ApngWrapper {

    private int resourceId;

    private List<ApngFrameResource> frameList = new ArrayList<>();

    private int loopCount = 0;

    private int width;

    private int height;

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public List<ApngFrameResource> getFrameList() {
        return frameList;
    }

    public void setFrameList(List<ApngFrameResource> frameList) {
        this.frameList = frameList;
    }

    public int getLoopCount() {
        return loopCount;
    }

    public void setLoopCount(int loopCount) {
        this.loopCount = loopCount;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public static class ApngFrameResource {

        public int frameWidth;
        public int frameHeight;
        public int frameX;
        public int frameY;
        public int frameDuration;
        public final byte blend_op;
        public final byte dispose_op;
        public byte[] ihdrData;
        public List<Chunk> imageChunks = new ArrayList<>();
        public List<Chunk> prefixChunks = new ArrayList<>();
        public static final byte[] sPNGSignatures = {(byte) 137, 80, 78, 71, 13, 10, 26, 10};
        public static final byte[] sPNGEndChunk = {0, 0, 0, 0, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82};

        private static ThreadLocal<CRC32> sCRC32 = new ThreadLocal<>();

        public CRC32 getCRC32() {
            CRC32 crc32 = sCRC32.get();
            if (crc32 == null) {
                crc32 = new CRC32();
                sCRC32.set(crc32);
            }
            return crc32;
        }

        public ApngFrameResource(FCTLChunk fctlChunk) {
            blend_op = fctlChunk.blend_op;
            dispose_op = fctlChunk.dispose_op;
            frameDuration = fctlChunk.delay_num * 1000 / (fctlChunk.delay_den == 0 ? 100 : fctlChunk.delay_den);
            if (frameDuration < 10) {
            /*  Many annoying ads specify a 0 duration to make an image flash as quickly as  possible.
            We follow Safari and Firefox's behavior and use a duration of 100 ms for any frames that specify a duration of <= 10 ms.
            See <rdar://problem/7689300> and <http://webkit.org/b/36082> for more information.
            See also: http://nullsleep.tumblr.com/post/16524517190/animated-gif-minimum-frame-delay-browser.
            */
                frameDuration = 100;
            }
            frameWidth = fctlChunk.width;
            frameHeight = fctlChunk.height;
            frameX = fctlChunk.x_offset;
            frameY = fctlChunk.y_offset;
        }

        @Override
        public String toString() {
            return "ApngFrameResource{" +
                    "frameWidth=" + frameWidth +
                    ", frameHeight=" + frameHeight +
                    ", frameX=" + frameX +
                    ", frameY=" + frameY +
                    ", frameDuration=" + frameDuration +
                    ", blend_op=" + blend_op +
                    ", dispose_op=" + dispose_op +
                    ", ihdrData=" + Arrays.toString(ihdrData) +
                    ", imageChunks=" + imageChunks +
                    ", prefixChunks=" + prefixChunks +
                    '}';
        }
    }
}
