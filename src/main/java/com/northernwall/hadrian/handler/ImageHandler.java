package com.northernwall.hadrian.handler;

import com.google.gson.Gson;
import com.northernwall.hadrian.db.DataAccess;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            String name = item.getFieldName();
            InputStream stream = item.openStream();
            if (item.isFormField()) {
                System.out.println("Form field '" + name + "' with value '" + Streams.asString(stream) + "' detected.");
            } else {
                System.out.println("File field '" + name + "' with file name '" + item.getName() + "' detected.");
                item.openStream();
            }
        }
    }

}
