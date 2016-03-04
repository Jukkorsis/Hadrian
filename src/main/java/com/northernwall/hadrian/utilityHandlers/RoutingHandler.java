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
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoutingHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(RoutingHandler.class);

    private final List<RouteEntry> routes;

    public RoutingHandler() {
        routes = new LinkedList<>();
    }

    public void addRoute(String method, RouteType type, String target, Handler handler) {
        routes.add(new RouteEntry(method, type, target, handler));
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        for (RouteEntry entry : routes) {
            if (entry.method.equals(request.getMethod())) {
                if ((entry.type == RouteType.equals && entry.target.equals(target))
                        || (entry.type == RouteType.startWith && entry.target.startsWith(target))
                        || (entry.type == RouteType.matches && entry.target.matches(target))) {
                    logger.info("{} handling {} request {}", entry.name, entry.method, target);
                    entry.handler.handle(target, request, httpRequest, response);
                    if (request.isHandled()) {
                        return;
                    }
                }
            }
        }
    }

}
