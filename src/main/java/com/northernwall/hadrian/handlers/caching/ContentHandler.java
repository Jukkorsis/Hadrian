/*
 * Copyright 2014 Richard Thurston.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.northernwall.hadrian.handlers.caching;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class ContentHandler extends AbstractHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ContentHandler.class);

    private final String rootPath;
    private final String indexPath;
    private final String webRootPath;
    private final int webRootLen;
    private final HtmlCompressor compressor;
    private final LoadingCache<String, CachedContent> cache;

    public ContentHandler(String rootPath, String webRootPath) {
        this.rootPath = rootPath;
        indexPath = rootPath + "/index.html";
        this.webRootPath = webRootPath;
        webRootLen = webRootPath.length()-1;
        compressor = new HtmlCompressor();
        cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .build(new CacheLoader<String, CachedContent>() {
                    @Override
                    public CachedContent load(String key) throws IOException {
                        try (InputStream is = this.getClass().getResourceAsStream(key)) {
                            if (is == null) {
                                return null;
                            }
                            return new CachedContent(key, is, compressor);
                        }
                    }
                });
    }
    
    public void preload(String resource) {
        try {
            cache.get(rootPath + resource);
        } catch (ExecutionException ex) {
            LOGGER.warn("Failed to preload {}, {}", resource, ex.getMessage());
        }
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String path;
        if (target.equals(webRootPath)) {
            path = indexPath;
        } else {
            path = rootPath + target.substring(webRootLen);
        }
        if (getContent(response, path)) {
            response.setStatus(200);
            request.setHandled(true);
        }
    }

    private boolean getContent(HttpServletResponse response, String resource) {
        try {
            CachedContent content = cache.get(resource);
            if (content == null) {
                LOGGER.warn("Could not get content {}", resource);
                return false;
            }

            if (resource.toLowerCase().endsWith(".html")) {
                response.addHeader("X-Frame-Options", "DENY");
                response.setContentType("text/html; charset=utf-8");
            }

            content.write(response.getOutputStream());
             return true;
        } catch (Exception ex) {
            return false;
        }
    }

}
