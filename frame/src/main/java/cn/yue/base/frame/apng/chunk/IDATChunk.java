package cn.yue.base.frame.apng.chunk;

/**
 * 默认显示图片从IDAT块取得
 */
public class IDATChunk extends Chunk {
    public static final int ID = Chunk.fourCCToInt("IDAT");
}
