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

package com.northernwall.hadrian.handler;

import com.google.gson.Gson;
import com.northernwall.hadrian.db.DataAccess;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class ImageHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(ImageHandler.class);

    private final DataAccess dataAccess;
    private final Gson gson;

    public ImageHandler(DataAccess dataAccess, Gson gson) {
        this.dataAccess = dataAccess;
        this.gson = gson;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.matches("/services/\\w+/image.json")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "POST":
                        updateImage(request, target.substring(10, target.length() - 11));
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
            } else if (target.matches("/services/\\w+/image/.+")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "GET":
                        int i = target.indexOf("/",10);
                        String serviceId = target.substring(10, i);
                        String name = target.substring(i+7);
                        logger.info("service '{}' name '{}'", serviceId, name);
                        getImage(serviceId, name, response);
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void updateImage(Request request, String serviceId) throws IOException, FileUploadException {
        if (!ServletFileUpload.isMultipartContent(request)) {
            logger.warn("Trying to upload image for {} but content is not multipart", serviceId);
            return;
        }
        logger.info("Trying to upload image for {}", serviceId);
        ServletFileUpload upload = new ServletFileUpload();

        // Parse the request
        FileItemIterator iter = upload.getItemIterator(request);
        while (iter.hasNext()) {
            FileItemStream item = iter.next();
            if (!item.isFormField()) {
                String name = item.getName();
                name = name.replace(' ', '-')
                        .replace('&', '-')
                        .replace('<', '-')
                        .replace('>', '-')
                        .replace('/', '-')
                        .replace('\\', '-')
                        .replace('&', '-')
                        .replace('@', '-')
                        .replace('?', '-')
                        .replace('^', '-')
                        .replace('#', '-')
                        .replace('%', '-')
                        .replace('=', '-')
                        .replace('$', '-')
                        .replace('{', '-')
                        .replace('}', '-')
                        .replace('[', '-')
                        .replace(']', '-')
                        .replace('|', '-')
                        .replace(';', '-')
                        .replace(':', '-')
                        .replace('~', '-')
                        .replace('`', '-');
                dataAccess.uploadImage(serviceId, name, item.getContentType(), item.openStream());
            }
        }
    }

    private void getImage(String serviceId, String name, HttpServletResponse response) throws IOException {
        byte[] buffer = new byte[1024];
        try (InputStream is = dataAccess.downloadImage(serviceId, name)) {
            if (is == null) {
                throw new RuntimeException("Can not find attachment '" + name + "' on service '" + serviceId + "'");
            }
            int len = is.read(buffer);
            while (len != -1) {
                response.getOutputStream().write(buffer, 0, len);
                len = is.read(buffer);
            }
        }
        response.setStatus(200);
    }

}
