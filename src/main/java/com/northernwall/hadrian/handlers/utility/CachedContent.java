package com.northernwall.hadrian.handlers.utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletOutputStream;
import org.slf4j.LoggerFactory;

public class CachedContent {
    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CachedContent.class);

    private final String resource;
    private final byte[] bytes;

    public CachedContent(String resource, InputStream inputStream) throws IOException {
        this.resource = resource;
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        byte[] buffer = new byte[50 * 1024];
        int len = inputStream.read(buffer);
        while (len != -1) {
            outputStream.write(buffer, 0, len);
            len = inputStream.read(buffer);
        }
        bytes = outputStream.toByteArray();
    }

    public void write(ServletOutputStream outputStream) {
        try {
            outputStream.write(bytes);
        } catch (Exception ex) {
            LOGGER.warn("Exception while writing content {}, {}", resource, ex.getMessage());
        }
        try {
            outputStream.flush();
        } catch (Exception ex) {
            LOGGER.warn("Exception while flushing content {}, {}", resource, ex.getMessage());
        }
    }

    public int getSize() {
        return bytes.length;
    }

}
