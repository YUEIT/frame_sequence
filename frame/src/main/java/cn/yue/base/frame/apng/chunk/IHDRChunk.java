package cn.yue.base.frame.apng.chunk;

/**
 * 包含Apng图片的宽、高等信息
 */
public class IHDRChunk extends Chunk {
    public static final int ID = Chunk.fourCCToInt("IHDR");
    /**
     * 图像宽度，以像素为单位
     */
    public int width;
    /**
     * 图像高度，以像素为单位
     */
    public int height;

    public byte[] data = new byte[5];
}
