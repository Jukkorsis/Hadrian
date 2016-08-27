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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.northernwall.hadrian.Const;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;

public class MetricHandler extends AbstractHandler {

    private final HandlerList handlers;
    private final Timer timerGet;
    private final Timer timerPost;
    private final Timer timerPut;
    private final Timer timerDelete;
    private final Meter meter200;
    private final Meter meter400;
    private final Meter meter500;

    public MetricHandler(HandlerList handlers, MetricRegistry metricRegistry) {
        this.handlers = handlers;
        timerGet = metricRegistry.timer("http.get");
        timerPost = metricRegistry.timer("http.post");
        timerPut = metricRegistry.timer("http.put");
        timerDelete = metricRegistry.timer("http.delete");
        meter200 = metricRegistry.meter("http.200");
        meter400 = metricRegistry.meter("http.400");
        meter500 = metricRegistry.meter("http.500");
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        Context context = null;
        switch (request.getMethod()) {
            case Const.HTTP_GET:
                context = timerGet.time();
                break;
            case Const.HTTP_POST:
                context = timerPost.time();
                break;
            case Const.HTTP_PUT:
                context = timerPut.time();
                break;
            case Const.HTTP_DELETE:
                context = timerDelete.time();
                break;
        }
        try {
            handlers.handle(target, request, httpRequest, response);
            
            if (response.getStatus() < 400) {
                meter200.mark();
            } else if (response.getStatus() < 500) {
                meter400.mark();
            } else {
                meter500.mark();
            }
        } finally {
            if (context != null) {
                context.stop();
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
