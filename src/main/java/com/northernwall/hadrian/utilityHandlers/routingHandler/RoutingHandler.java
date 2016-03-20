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
package com.northernwall.hadrian.utilityHandlers.routingHandler;

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

    private final List<RouteEntry> getRoutes;
    private final List<RouteEntry> putRoutes;
    private final List<RouteEntry> postRoutes;
    private final List<RouteEntry> deleteRoutes;

    public RoutingHandler() {
        getRoutes = new LinkedList<>();
        putRoutes = new LinkedList<>();
        postRoutes = new LinkedList<>();
        deleteRoutes = new LinkedList<>();
    }

    public void addRoute(MethodRule methodRule, TargetRule targetRule, String targetPattern, Handler handler) {
        add(methodRule, targetRule, targetPattern, handler, true);
    }

    public void addUtilityRoute(MethodRule methodRule, TargetRule targetRule, String targetPattern, Handler handler) {
        add(methodRule, targetRule, targetPattern, handler, false);
    }

    private void add(MethodRule methodRule, TargetRule targetRule, String targetPattern, Handler handler, boolean logAccess) {
        RouteEntry entry = new RouteEntry(targetRule, targetPattern, handler, logAccess);
        switch (methodRule) {
            case GET:
                getRoutes.add(entry);
                break;
            case PUT:
                putRoutes.add(entry);
                break;
            case POST:
                postRoutes.add(entry);
                break;
            case DELETE:
                deleteRoutes.add(entry);
                break;
            case PUTPOST:
                putRoutes.add(entry);
                postRoutes.add(entry);
                break;
            case ANY:
                getRoutes.add(entry);
                putRoutes.add(entry);
                postRoutes.add(entry);
                deleteRoutes.add(entry);
                break;
        }
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        List<RouteEntry> routes;
        switch (request.getMethod()) {
            case "GET":
                routes = getRoutes;
                break;
            case "PUT":
                routes = putRoutes;
                break;
            case "POST":
                routes = postRoutes;
                break;
            case "DELETE":
                routes = deleteRoutes;
                break;
            default:
                return;
        }
        for (RouteEntry entry : routes) {
            if (entry.targetRule.test(entry.targetPattern, target)) {
                try {
                    if (entry.logAccess) {
                        logger.info("{} handling {} request for {}", entry.name, request.getMethod(), target);
                    }
                    entry.handler.handle(target, request, httpRequest, response);
                    if (request.isHandled()) {
                        return;
                    }
                } catch (HttpAbstractException e) {
                    logger.error("Exception '{}' while {} was handling {} request for {}", e.getMessage(), entry.name, request.getMethod(), target);
                    response.getWriter().print(e.getMessage());
                    response.setStatus(e.getStatus());
                    request.setHandled(true);
                    return;
                } catch (Exception e) {
                    logger.error("Exception '{}' while {} was handling {} request for {}", e.getMessage(), entry.name, request.getMethod(), target, e);
                    response.getWriter().print("Internal Server Error.");
                    response.setStatus(500);
                    request.setHandled(true);
                    return;
                }
            }
        }
        logger.info("Could not find a handler for {} {}", request.getMethod(), target);
    }

}
