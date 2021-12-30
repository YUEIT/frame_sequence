package cn.yue.base.frame.apng.chunk;

import java.io.IOException;

import cn.yue.base.frame.apng.io.APNGReader;

/**
 * 包含帧的一些控制信息，如Apng总的帧数，循环播放的次数
 * fcTL是每帧的控制信息块，fcTL一定是出现在fdAT或者IDAT的前面
 */
public class FCTLChunk extends Chunk {
    public static final int ID = fourCCToInt("fcTL");
    /**
     * 控制帧的序号，从0开始
     */
    public int sequence_number;
    /**
     * 帧的宽度
     */
    public int width;
    /**
     * 帧的高度
     */
    public int height;
    /**
     * 在x方向的偏移
     */
    public int x_offset;
    /**
     * 在y方向的偏移
     */
    public int y_offset;
    /**
     * 帧动画时间间隙的分子
     */
    public short delay_num;
    /**
     * 帧动画时间间隙的分母
     */
    public short delay_den;
    /**
     * 在显示该帧之前，需要对前面缓冲输出区域做何种处理
     */
    public byte dispose_op;
    /**
     * 具体显示该帧的方式
     */
    public byte blend_op;

    /**
     * 不做任何处理
     */
    public static final int APNG_DISPOSE_OP_NON = 0;

    /**
     * 前一帧的x方向的偏移、y方向的偏移和当前帧的宽、高做一个剪裁，并将剪裁的区域抠成全黑色透明
     */
    public static final int APNG_DISPOSE_OP_BACKGROUND = 1;

    /**
     * 将当前缓冲输出区域恢复到先前的内容区域
     */
    public static final int APNG_DISPOSE_OP_PREVIOUS = 2;

    /**
     * 通过dispose处理后得到的bitmap，在该bitmap对当前帧的x方向的偏移、y方向的偏移和当前帧的宽、高做一个剪裁，
     * 并将剪裁的区域抠成全黑色透明，最后将当前帧写到该剪裁区域上
     */
    public static final int APNG_BLEND_OP_SOURCE = 0;

    /**
     * 当前帧覆盖到当前的缓冲区域并显示
     */
    public static final int APNG_BLEND_OP_OVER = 1;

}
