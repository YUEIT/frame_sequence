package cn.yue.base.frame.apng.loader;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.yue.base.frame.apng.chunk.ACTLChunk;
import cn.yue.base.frame.apng.chunk.Chunk;
import cn.yue.base.frame.apng.chunk.FCTLChunk;
import cn.yue.base.frame.apng.chunk.FDATChunk;
import cn.yue.base.frame.apng.chunk.IDATChunk;
import cn.yue.base.frame.apng.chunk.IENDChunk;
import cn.yue.base.frame.apng.chunk.IHDRChunk;
import cn.yue.base.frame.apng.io.APNGReader;

/**
 * Description :
 * Created by yue on 2021/12/8
 */

public class ApngParser {

    public static ApngWrapper parse(APNGReader reader) {
        try {
            ApngWrapper apngWrapper = new ApngWrapper();
            List<Chunk> chunks = parseFrame(reader);
            List<Chunk> otherChunks = new ArrayList<>();

            boolean actl = false;
            ApngWrapper.ApngFrameResource lastFrame = null;
            byte[] ihdrData = new byte[0];
            List<ApngWrapper.ApngFrameResource> frameList = new ArrayList<>();
            for (Chunk chunk : chunks) {
                if (chunk instanceof ACTLChunk) {
                    apngWrapper.setLoopCount(((ACTLChunk) chunk).num_plays);
                    actl = true;
                } else if (chunk instanceof FCTLChunk) {
                    ApngWrapper.ApngFrameResource frame = new ApngWrapper.ApngFrameResource((FCTLChunk) chunk);
                    frame.prefixChunks = otherChunks;
                    frame.ihdrData = ihdrData;
                    frameList.add(frame);
                    lastFrame = frame;
                } else if (chunk instanceof FDATChunk) {
                    if (lastFrame != null) {
                        lastFrame.imageChunks.add(chunk);
                    }
                } else if (chunk instanceof IDATChunk) {
                    if (!actl) {
                        //如果为非APNG图片，则只解码PNG
//                    Frame frame = new StillFrame(reader);
//                    frame.frameWidth = canvasWidth;
//                    frame.frameHeight = canvasHeight;
                        break;
                    }
                    if (lastFrame != null) {
                        lastFrame.imageChunks.add(chunk);
                    }
                } else if (chunk instanceof IHDRChunk) {
                    ihdrData = ((IHDRChunk) chunk).data;
                    apngWrapper.setWidth(((IHDRChunk) chunk).width);
                    apngWrapper.setHeight(((IHDRChunk) chunk).height);
                    Log.d("luobiao", "parse: " + ((IHDRChunk) chunk).width + ";" + ((IHDRChunk) chunk).height);
                } else if (!(chunk instanceof IENDChunk)) {
                    otherChunks.add(chunk);
                }
            }
            apngWrapper.setFrameList(frameList);
            return apngWrapper;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Chunk> parseFrame(APNGReader reader) throws IOException {
        if (!reader.matchFourCC("\u0089PNG") || !reader.matchFourCC("\r\n\u001a\n")) {
            throw new IOException();
        }
        List<Chunk> chunks = new ArrayList<>();
        while (reader.available() > 0) {
            chunks.add(parseChunk(reader));
        }
        return chunks;
    }

    private static Chunk parseChunk(APNGReader reader) throws IOException {
        int offset = reader.position();
        int size = reader.readInt();
        int fourCC = reader.readFourCC();
        if (fourCC == ACTLChunk.ID) {
            return parseACTLChunk(offset, size, fourCC, reader);
        } else if (fourCC == FCTLChunk.ID) {
            return parseFCTLChunk(offset, size, fourCC, reader);
        } else if (fourCC == FDATChunk.ID) {
            return parseFDATChunk(offset, size, fourCC, reader);
        } else if (fourCC == IDATChunk.ID) {
            return parseIDATChunk(offset, size, fourCC, reader);
        } else if (fourCC == IENDChunk.ID) {
            return parseIENDChunk(offset, size, fourCC, reader);
        } else if (fourCC == IHDRChunk.ID) {
            return parseIHDRChunk(offset, size, fourCC, reader);
        } else {
            return parseSimpleChunk(offset, size, fourCC, reader);
        }
    }

    public static Chunk parseACTLChunk(int offset, int length, int fourCC, APNGReader reader) throws IOException {
        ACTLChunk chunk = new ACTLChunk();
        chunk.offset = offset;
        chunk.fourcc = fourCC;
        chunk.length = length;
        int available = reader.available();
        chunk.num_frames = reader.readInt();
        chunk.num_plays = reader.readInt();
        int offsetBit = available - reader.available();
        if (offsetBit > length) {
            throw new IOException("Out of chunk area");
        } else if (offsetBit < length) {
            reader.skip(length - offsetBit);
        }
        chunk.crc = reader.readInt();
        return chunk;
    }

    private static Chunk parseFCTLChunk(int offset, int length, int fourCC, APNGReader reader) throws IOException {
        FCTLChunk chunk = new FCTLChunk();
        chunk.offset = offset;
        chunk.fourcc = fourCC;
        chunk.length = length;
        int available = reader.available();
        chunk.sequence_number = reader.readInt();
        chunk.width = reader.readInt();
        chunk.height = reader.readInt();
        chunk.x_offset = reader.readInt();
        chunk.y_offset = reader.readInt();
        chunk.delay_num = reader.readShort();
        chunk.delay_den = reader.readShort();
        chunk.dispose_op = reader.peek();
        chunk.blend_op = reader.peek();
        int offsetBit = available - reader.available();
        if (offsetBit > length) {
            throw new IOException("Out of chunk area");
        } else if (offsetBit < length) {
            reader.skip(length - offsetBit);
        }
        chunk.crc = reader.readInt();
        return chunk;
    }

    private static Chunk parseFDATChunk(int offset, int length, int fourCC, APNGReader reader) throws IOException {
        FDATChunk chunk = new FDATChunk();
        chunk.offset = offset;
        chunk.fourcc = fourCC;
        chunk.length = length;
        int available = reader.available();
        chunk.sequence_number = reader.readInt();
        int offsetBit = available - reader.available();
        if (offsetBit > length) {
            throw new IOException("Out of chunk area");
        } else if (offsetBit < length) {
            reader.skip(length - offsetBit);
        }
        chunk.crc = reader.readInt();
        return chunk;
    }

    private static Chunk parseIDATChunk(int offset, int length, int fourCC, APNGReader reader) throws IOException {
        IDATChunk chunk = new IDATChunk();
        chunk.offset = offset;
        chunk.fourcc = fourCC;
        chunk.length = length;
        int available = reader.available();
        int offsetBit = available - reader.available();
        if (offsetBit > length) {
            throw new IOException("Out of chunk area");
        } else if (offsetBit < length) {
            reader.skip(length - offsetBit);
        }
        chunk.crc = reader.readInt();
        return chunk;
    }

    private static Chunk parseIENDChunk(int offset, int length, int fourCC, APNGReader reader) throws IOException {
        IENDChunk chunk = new IENDChunk();
        chunk.offset = offset;
        chunk.fourcc = fourCC;
        chunk.length = length;
        int available = reader.available();
        int offsetBit = available - reader.available();
        if (offsetBit > length) {
            throw new IOException("Out of chunk area");
        } else if (offsetBit < length) {
            reader.skip(length - offsetBit);
        }
        chunk.crc = reader.readInt();
        return chunk;
    }

    private static Chunk parseIHDRChunk(int offset, int length, int fourCC, APNGReader reader) throws IOException {
        IHDRChunk chunk = new IHDRChunk();
        chunk.offset = offset;
        chunk.fourcc = fourCC;
        chunk.length = length;
        int available = reader.available();
        chunk.width = reader.readInt();
        chunk.height = reader.readInt();
        reader.read(chunk.data, 0, chunk.data.length);
        int offsetBit = available - reader.available();
        if (offsetBit > length) {
            throw new IOException("Out of chunk area");
        } else if (offsetBit < length) {
            reader.skip(length - offsetBit);
        }
        chunk.crc = reader.readInt();
        return chunk;
    }

    private static Chunk parseSimpleChunk(int offset, int length, int fourCC, APNGReader reader) throws IOException {
        Chunk chunk = new Chunk();
        chunk.offset = offset;
        chunk.fourcc = fourCC;
        chunk.length = length;
        int available = reader.available();
        int offsetBit = available - reader.available();
        if (offsetBit > length) {
            throw new IOException("Out of chunk area");
        } else if (offsetBit < length) {
            reader.skip(length - offsetBit);
        }
        chunk.crc = reader.readInt();
        return chunk;
    }
}
