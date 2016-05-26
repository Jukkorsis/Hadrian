package com.northernwall.hadrian.utilityHandlers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletOutputStream;

public class CachedContent {
    private final byte[] bytes;

    public CachedContent(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        byte[] buffer = new byte[50 * 1024];
        int len = inputStream.read(buffer);
        while (len != -1) {
            outputStream.write(buffer, 0, len);
            len = inputStream.read(buffer);
        }
        bytes = outputStream.toByteArray();
    }

    public void write(ServletOutputStream outputStream) throws IOException {
        outputStream.write(bytes);
    }

}
