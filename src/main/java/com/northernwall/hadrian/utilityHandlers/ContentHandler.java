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

package com.northernwall.hadrian.utilityHandlers;

import com.northernwall.hadrian.Const;
import java.io.IOException;
import java.io.InputStream;
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

    private final static Logger logger = LoggerFactory.getLogger(ContentHandler.class);
    
    private final String rootPath;

    public ContentHandler(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (!request.getMethod().equals(Const.HTTP_GET)) {
                request.setHandled(false);
                return;
            }
            if (target.equals("/ui/")) {
                if (getContent(response, rootPath + "/index.html")) {
                    logger.info("Handled {} request {} with root path {}", request.getMethod(), target, rootPath);
                    response.setStatus(200);
                    request.setHandled(true);
                }
            } else if (target.startsWith("/ui/")) {
                if (getContent(response, rootPath + target.substring(3))) {
                    logger.debug("Handled {} request {} root path {}", request.getMethod(), target, rootPath);
                    response.setStatus(200);
                    request.setHandled(true);
                }
            } else if (target.equals("/favicon.ico")) {
                response.setStatus(200);
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private boolean getContent(HttpServletResponse response, String resource) throws IOException {
        byte[] buffer = new byte[50*1024];
        try (InputStream is = this.getClass().getResourceAsStream(resource)) {
            if (is == null) {
                return false;
            }
            if (resource.toLowerCase().endsWith(".html")) {
                response.addHeader("X-Frame-Options", "DENY");
                response.setContentType(Const.HTML);
            }
            int len = is.read(buffer);
            while (len != -1) {
                response.getOutputStream().write(buffer, 0, len);
                len = is.read(buffer);
            }
        }
        return true;
    }

}
