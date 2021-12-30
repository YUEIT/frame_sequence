package com.bumptech.glide.integration.webp.decoder;

import java.io.InputStream;

/**
 * Description :
 * Created by yue on 2021/12/7
 */

public class WrapInputStream {

    private InputStream inputStream;

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
