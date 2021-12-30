package cn.yue.base.frame.apng.chunk;


/**
 * 包含动画播放的控制信息
 */
public class ACTLChunk extends Chunk {
    public static final int ID = Chunk.fourCCToInt("acTL");
    /**
     * 0~3字节表示该Apng总的播放帧数
     */
    public int num_frames;
    /**
     * 4~7字节表示该Apng循环播放的次数
     */
    public int num_plays;
}
