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
public class AvailabilityHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(AvailabilityHandler.class);

    public AvailabilityHandler() {
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.equals("/availablity")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                response.setStatus(200);
                request.setHandled(true);
            } else if (target.equals("/version")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                response.setStatus(200);
                String version = "0.1.0";
                response.getOutputStream().write(version.getBytes());
                request.setHandled(true);
            } else if (target.equals("/health")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                response.setStatus(200);
                String version = "Good";
                response.getOutputStream().write(version.getBytes());
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

}
