package com.bumptech.glide.integration.webp.decoder;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.integration.webp.WebpHeaderParser;
import com.bumptech.glide.load.Option;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


/**
 * An {@link ResourceDecoder} that decodes {@link
 *  WebpDrawable} from {@link InputStream} data
 *
 * @author liuchun
 */
public class WrapStreamWebpDecoder implements ResourceDecoder<WrapInputStream, WebpDrawable> {
    public static final Option<Boolean> DISABLE_ANIMATION = Option.memory(
            "com.bumptech.glide.integration.webp.decoder.StreamWebpDecoder.DisableAnimation", false);

    private final ResourceDecoder<ByteBuffer, WebpDrawable> byteBufferDecoder;
    private final ArrayPool byteArrayPool;

    public WrapStreamWebpDecoder(ResourceDecoder<ByteBuffer, WebpDrawable> byteBufferDecoder, ArrayPool byteArrayPool) {
        this.byteBufferDecoder = byteBufferDecoder;
        this.byteArrayPool = byteArrayPool;
    }


    @Override
    public boolean handles(@NonNull WrapInputStream inputStream, @NonNull Options options) throws IOException {
        Log.d("luobiao", "handles:1 ");
        return true;
//        if (options.get(DISABLE_ANIMATION)) {
//            return false;
//        }
//        WebpHeaderParser.WebpImageType webpType = WebpHeaderParser.getType(inputStream.getInputStream(), byteArrayPool);
//        return WebpHeaderParser.isAnimatedWebpType(webpType);
    }

    @Nullable
    @Override
    public Resource<WebpDrawable> decode(@NonNull WrapInputStream inputStream, int width, int height, @NonNull Options options) throws IOException {

        byte[] data = Utils.inputStreamToBytes(inputStream.getInputStream());
        if (data == null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);

        return byteBufferDecoder.decode(byteBuffer, width, height, options);
    }
}
