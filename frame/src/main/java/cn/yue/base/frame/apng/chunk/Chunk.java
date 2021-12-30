package cn.yue.base.frame.apng.chunk;

import android.text.TextUtils;

/**
 *
 * Chunk由四部分组成:
 *  数据长度（4个字节）指定数据块中数据域的长度，其长度不超过(231－1)字节
 *  Chunk类型（4个字节）数据块类型码由ASCII字母(A-Z和a-z)组成
 *  ChunkData 可变长度	存储按照Chunk Type Code指定的数据
 *  CRC（循环冗余校验，4个字节）存储用来检测是否有错误的循环冗余码
*/

public class Chunk {
    public int length;
    public int fourcc;
    public int crc;
    public int offset;

    public static int fourCCToInt(String fourCC) {
        if (TextUtils.isEmpty(fourCC) || fourCC.length() != 4) {
            return 0xbadeffff;
        }
        return (fourCC.charAt(0) & 0xff)
                | (fourCC.charAt(1) & 0xff) << 8
                | (fourCC.charAt(2) & 0xff) << 16
                | (fourCC.charAt(3) & 0xff) << 24
                ;
    }
}
