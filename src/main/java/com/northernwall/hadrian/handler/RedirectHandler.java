package com.northernwall.hadrian.handler;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedirectHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(RedirectHandler.class);

    public RedirectHandler() {
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            logger.info("Handling {} request {}", request.getMethod(), target);
            redirect(response);
            response.setStatus(200);
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        } finally {
            request.setHandled(true);
        }
    }

    private void redirect(HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        response.getOutputStream().print("<html><head><meta http-equiv=\"refresh\" content=\"1;url=/ui/\"></head><body></body></html>");
    }

}
