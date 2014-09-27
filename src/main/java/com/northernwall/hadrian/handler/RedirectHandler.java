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

import java.io.IOException;
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
