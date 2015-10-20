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

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private final static String VERSION = "0.1.1";

    public AvailabilityHandler() {
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.equals("/availability")) {
                //logger.info("Handling {} request {}", request.getMethod(), target);
                response.setStatus(200);
                request.setHandled(true);
            } else if (target.equals("/version")) {
                //logger.info("Handling {} request {}", request.getMethod(), target);
                response.setStatus(200);
                String version = VERSION; //getClass().getPackage().getImplementationVersion();
                response.getOutputStream().write(version.getBytes());
                request.setHandled(true);
            } else if (target.equals("/health")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                response.setStatus(200);
                writeln(response, "<html>");
                writeln(response, "<body>");
                writeln(response, "Version", VERSION);
                writeln(response, "JVM Name", ManagementFactory.getRuntimeMXBean().getSpecName());
                writeln(response, "JVM Version", ManagementFactory.getRuntimeMXBean().getSpecVersion());
                writeln(response, "JVM Vendor", ManagementFactory.getRuntimeMXBean().getSpecVendor());
                writeln(response, "JVM Threads", ManagementFactory.getThreadMXBean().getThreadCount());
                writeln(response, "Current Time", new Date());
                writeln(response, "Start Time", new Date(ManagementFactory.getRuntimeMXBean().getStartTime()));
                writeln(response, "</body>");
                writeln(response, "</html>");
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void writeln(HttpServletResponse response, String text) throws IOException {
        response.getOutputStream().write(text.getBytes());
    }

    private void writeln(HttpServletResponse response, String label, Date value) throws IOException {
        writeln(response, label, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value));
    }

    private void writeln(HttpServletResponse response, String label, int value) throws IOException {
        writeln(response, label, Integer.toString(value));
    }

    private void writeln(HttpServletResponse response, String label, long value) throws IOException {
        writeln(response, label, Long.toString(value));
    }

    private void writeln(HttpServletResponse response, String label, String value) throws IOException {
        String text = label + ": " + value + "<br/>";
        response.getOutputStream().write(text.getBytes());
    }

}
