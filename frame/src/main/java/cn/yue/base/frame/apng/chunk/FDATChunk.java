package cn.yue.base.frame.apng.chunk;

/**
 * 包含每帧的图片数据信息
 */
public class FDATChunk extends Chunk {
    public static final int ID = Chunk.fourCCToInt("fdAT");
    /**
     * 0~3字节表示动画帧的编号，从0开始
     */
    public int sequence_number;
}
