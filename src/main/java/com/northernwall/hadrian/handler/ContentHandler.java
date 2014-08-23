package com.northernwall.hadrian.handler;

import com.google.gson.Gson;
import com.northernwall.hadrian.SoaRepDataAccess;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(ContentHandler.class);

    public ContentHandler() {
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.equals("/")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                redirect(response);
                response.setStatus(200);
                request.setHandled(true);
            } else if (target.equals("/availablity")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                response.setStatus(200);
                request.setHandled(true);
            } else if (target.equals("/ui/")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                getContent(response, "/webapp/index.html");
                response.setStatus(200);
                request.setHandled(true);
            } else if (target.startsWith("/ui/")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                getContent(response, "/webapp" + target.substring(3));
                response.setStatus(200);
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void redirect(HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        response.getOutputStream().print("<html><head><meta http-equiv=\"refresh\" content=\"1;url=/ui/\"></head><body></body></html>");
        response.setStatus(200);
    }

    private void getContent(HttpServletResponse response, String resource) throws IOException {
        byte[] buffer = new byte[1024];
        try (InputStream is = this.getClass().getResourceAsStream(resource)) {
            if (is == null) {
                throw new RuntimeException("Can not find resource '" + resource + "'");
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
