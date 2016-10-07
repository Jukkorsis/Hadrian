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
package com.northernwall.hadrian.handlers.utility;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.cache.LoadingCache;
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
    private final LoadingCache<String, CachedContent> cache;

    public ContentHandler(String rootPath) {
        this.rootPath = rootPath;
        indexPath = rootPath + "/index.html";
        cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .build(new CacheLoader<String, CachedContent>() {
                    @Override
                    public CachedContent load(String key) throws IOException {
                        try (InputStream is = this.getClass().getResourceAsStream(key)) {
                            if (is == null) {
                                return null;
                            }
                            CachedContent content = new CachedContent(is);
                            LOGGER.info("Loaded content {} into cache, {} bytes", key, content.getSize());
                            return content;
                        }
                    }
                });
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String path;
        if (target.equals("/ui/")) {
            path = indexPath;
        } else {
            path = rootPath + target.substring(3);
        }
        if (getContent(response, path)) {
            response.setStatus(200);
            request.setHandled(true);
        }
    }

    private boolean getContent(HttpServletResponse response, String resource) throws ServletException {
        try {
            CachedContent content = cache.get(resource);

            if (resource.toLowerCase().endsWith(".html")) {
                response.addHeader("X-Frame-Options", "DENY");
                response.setContentType("text/html; charset=utf-8");
            }

            content.write(response.getOutputStream());
            return true;
        } catch (InvalidCacheLoadException | ExecutionException | IOException ex) {
            return false;
        }
    }

}
