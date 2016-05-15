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
package com.northernwall.hadrian.graph;

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ModuleRef;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 *
 * @author Richard Thurston
 */
public class GraphFanInHandler extends AbstractHandler {

    private final DataAccess dataAccess;

    public GraphFanInHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String serviceId = target.substring(16);

        response.setContentType(Const.TEXT);
        Graph graph = new Graph(response.getOutputStream());

        List<Service> services = new LinkedList<>();
        List<String> foundIds = new LinkedList<>();
        Service service = dataAccess.getService(serviceId);
        services.add(service);
        foundIds.add(service.getServiceId());
        while (!services.isEmpty()) {
            fanIn(services.remove(0), graph, services, foundIds);
        }
        graph.newLine();
        graph.writeService(service, "rectangle", false);
        graph.close();

        request.setHandled(true);
        response.setStatus(200);
    }

    private void fanIn(Service service, Graph graph, List<Service> services, List<String> foundIds) throws IOException {
        List<ModuleRef> serviceRefs;
        /*
        serviceRefs = dataAccess.getModuleRefsByServer(service.getServiceId());
        if (serviceRefs != null && !serviceRefs.isEmpty()) {
            for (ModuleRef serviceRef : serviceRefs) {
                if (!foundIds.contains(serviceRef.getClientServiceId())) {
                    Service temp = dataAccess.getService(serviceRef.getClientServiceId());
                    graph.writeLink(temp.getServiceAbbr(), service.getServiceAbbr());
                    graph.writeService(temp, "ellipse", true);
                    services.add(temp);
                    foundIds.add(temp.getServiceId());
                }
            }
        }
        */
    }

}
