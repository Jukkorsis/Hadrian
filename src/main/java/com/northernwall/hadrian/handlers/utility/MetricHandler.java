/*
 * Copyright 2015 Richard Thurston.
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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dsh.metrics.MetricRegistry;
import org.dsh.metrics.Timer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;

public class MetricHandler extends AbstractHandler {

    private final HandlerList handlers;
    private final MetricRegistry metricRegistry;

    public MetricHandler(HandlerList handlers, MetricRegistry metricRegistry) {
        this.handlers = handlers;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        Timer timer = null;

        if (!target.equals("/availability")
                && !target.equals("/version")
                && !target.startsWith("/ui/")) {
            timer = metricRegistry.timer(
                    "http",
                    "url", target,
                    "method", request.getMethod());
        }

        try {
            handlers.handle(target, request, httpRequest, response);
        } finally {
            if (timer != null) {
                timer.stop("status", Integer.toString(response.getStatus()));
            }
        }
    }

    @Override
    public void setServer(Server server) {
        super.setServer(server);
        handlers.setServer(server);
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        handlers.stop();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        handlers.start();
    }

}
