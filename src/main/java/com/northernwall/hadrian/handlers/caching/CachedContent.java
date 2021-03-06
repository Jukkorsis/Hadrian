package com.northernwall.hadrian.handlers.caching;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletOutputStream;
import org.slf4j.LoggerFactory;

public class CachedContent {

    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CachedContent.class);
    
    public static String compressionJS(String origText) {
        int len = origText.length();
        StringBuilder b = new StringBuilder(len);
        char c = origText.charAt(0);
        for (int i = 1; i < len; i++) {
            char n = origText.charAt(i);
            if (c == ' ' && n == ' ') {
                c = n;
            } else if (c == '\n' && n == ' ') {
            } else if (c != ';' && n == '\n') {
            } else {
                b.append(c);
                c = n;
            }
        }
        b.append(c);
        return b.toString();
    }

    private final String resource;
    private final byte[] bytes;

    public CachedContent(String resource, InputStream inputStream, HtmlCompressor compressor) throws IOException {
        this.resource = resource;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[50 * 1024];
        int len = inputStream.read(buffer);
        while (len != -1) {
            outputStream.write(buffer, 0, len);
            len = inputStream.read(buffer);
        }
        if (resource.endsWith(".html")) {
            int origSize = outputStream.size();
            bytes = compressor.compress(outputStream.toString()).getBytes();
            LOGGER.info("Loaded content {} into cache, {} bytes, was {} bytes", resource, bytes.length, origSize);
        } else if ((resource.startsWith("/webapp/js/") || resource.startsWith("/webcontent/js/"))
                && resource.endsWith(".js")) {
            int origSize = outputStream.size();
            bytes = outputStream.toByteArray();
            //bytes = compressionJS(outputStream.toString()).getBytes();
            LOGGER.info("Loaded content {} into cache, {} bytes, was {} bytes", resource, bytes.length, origSize);
        } else {
            bytes = outputStream.toByteArray();
            LOGGER.info("Loaded content {} into cache, {} bytes", resource, bytes.length);
        }
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


}
