package cn.yue.base.frame.apng.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

import cn.yue.base.frame.apng.chunk.Chunk;
import cn.yue.base.frame.apng.chunk.FDATChunk;
import cn.yue.base.frame.apng.chunk.IDATChunk;
import cn.yue.base.frame.apng.chunk.IENDChunk;
import cn.yue.base.frame.apng.chunk.IHDRChunk;
import cn.yue.base.frame.apng.io.APNGReader;
import cn.yue.base.frame.apng.io.APNGWriter;
import cn.yue.base.frame.apng.io.StreamReader;

/**
 * Description :
 * Created by yue on 2021/12/8
 */

public class ApngFrameReader {

    private final Bitmap.Config config;
    private final ApngWrapper apngWrapper;
    private final Context context;
    private final APNGWriter apngWriter;
    private final APNGReader apngReader;
    private final ApngRender apngRender;

    public ApngFrameReader(Context context, ApngWrapper apngWrapper, Bitmap.Config config) {
        this.apngWrapper = apngWrapper;
        this.context = context;
        this.config = config;
        apngRender = new ApngRender();
        InputStream inputStream = context.getResources().openRawResource(apngWrapper.getResourceId());
        StreamReader streamReader = new StreamReader(inputStream);
        apngReader = new APNGReader(streamReader);
        apngWriter = new APNGWriter();
    }

    public void getFrame(int frameNr, final ApngDecoder.PackFrame output) {
        ApngWrapper.ApngFrameResource frame = apngWrapper.getFrameList().get(frameNr);
        if (output.frame == null) {
            output.frame = new ApngFrame();
        }
        Bitmap inBitmap = null;
        if (output.frame.getFrameSource() != null) {
            inBitmap = ((ApngFrame.FrameBitmap) output.frame.getFrameSource()).getBitmap();
        }
        Bitmap newBitmap = getBitmap(inBitmap, frame);
        output.frame.setSource(frame, newBitmap, apngRender);
    }

    public Bitmap getBitmap(Bitmap reusedBitmap, ApngWrapper.ApngFrameResource frameResource) {
        try {
            int length = encode(frameResource);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = config;
            options.inSampleSize = 1;
            options.inMutable = true;
            if (reusedBitmap != null) {
                options.inBitmap = reusedBitmap;
            }
            byte[] bytes = apngWriter.toByteArray();
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, length, options);
            } catch (IllegalArgumentException e) {
                // Problem decoding into existing bitmap when on Android 4.2.2 & 4.3
                BitmapFactory.Options optionsFixed = new BitmapFactory.Options();
                optionsFixed.inJustDecodeBounds = false;
                optionsFixed.inSampleSize = 1;
                optionsFixed.inMutable = true;
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, length, optionsFixed);
            }

            assert bitmap != null;
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int encode(ApngWrapper.ApngFrameResource frameResource) throws IOException {
        int fileSize = 8 + 13 + 12;

        //prefixChunks
        for (Chunk chunk : frameResource.prefixChunks) {
            fileSize += chunk.length + 12;
        }

        //imageChunks
        for (Chunk chunk : frameResource.imageChunks) {
            if (chunk instanceof IDATChunk) {
                fileSize += chunk.length + 12;
            } else if (chunk instanceof FDATChunk) {
                fileSize += chunk.length + 8;
            }
        }
        fileSize += frameResource.sPNGEndChunk.length;
        apngWriter.reset(fileSize);
        apngWriter.putBytes(frameResource.sPNGSignatures);
        //IHDR Chunk
        apngWriter.writeInt(13);
        int start = apngWriter.position();
        apngWriter.writeFourCC(IHDRChunk.ID);
        apngWriter.writeInt(frameResource.frameWidth);
        apngWriter.writeInt(frameResource.frameHeight);
        apngWriter.putBytes(frameResource.ihdrData);
        CRC32 crc32 = frameResource.getCRC32();
        crc32.reset();
        crc32.update(apngWriter.toByteArray(), start, 17);
        apngWriter.writeInt((int) crc32.getValue());

        //prefixChunks
        for (Chunk chunk : frameResource.prefixChunks) {
            if (chunk instanceof IENDChunk) {
                continue;
            }
            apngReader.reset();
            apngReader.skip(chunk.offset);
            apngReader.read(apngWriter.toByteArray(), apngWriter.position(), chunk.length + 12);
            apngWriter.skip(chunk.length + 12);
        }
        //imageChunks
        for (Chunk chunk : frameResource.imageChunks) {
            if (chunk instanceof IDATChunk) {
                apngReader.reset();
                apngReader.skip(chunk.offset);
                apngReader.read(apngWriter.toByteArray(), apngWriter.position(), chunk.length + 12);
                apngWriter.skip(chunk.length + 12);
            } else if (chunk instanceof FDATChunk) {
                apngWriter.writeInt(chunk.length - 4);
                start = apngWriter.position();
                apngWriter.writeFourCC(IDATChunk.ID);

                apngReader.reset();
                // skip to fdat data position
                apngReader.skip(chunk.offset + 4 + 4 + 4);
                apngReader.read(apngWriter.toByteArray(), apngWriter.position(), chunk.length - 4);

                apngWriter.skip(chunk.length - 4);
                crc32.reset();
                crc32.update(apngWriter.toByteArray(), start, chunk.length);
                apngWriter.writeInt((int) crc32.getValue());
            }
        }
        //endChunk
        apngWriter.putBytes(frameResource.sPNGEndChunk);
        return fileSize;
    }

    public int getFrameCount() {
        if (apngWrapper == null || apngWrapper.getFrameList() == null) {
            return 0;
        }
        return apngWrapper.getFrameList().size();
    }

    public long getDelay(int framePointer) {
        if (apngWrapper == null || apngWrapper.getFrameList() == null) {
            return 0;
        }
        return apngWrapper.getFrameList().get(framePointer).frameDuration;
    }

    public int getWidth() {
        return apngWrapper.getWidth();
    }

    public int getHeight() {
        return apngWrapper.getHeight();
    }

    public void release() {

    }

}
