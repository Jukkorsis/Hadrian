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

import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceRef;
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
public class GraphFanOutHandler extends AbstractHandler {

    private final DataAccess dataAccess;

    public GraphFanOutHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String serviceId = target.substring(17);

        Graph graph = new Graph(response, true);

        List<Service> services = new LinkedList<>();
        List<String> foundIds = new LinkedList<>();
        Service service = dataAccess.getService(serviceId);
        services.add(service);
        foundIds.add(service.getServiceId());
        while (!services.isEmpty()) {
            fanOut(services.remove(0), graph, services, foundIds);
        }
        graph.newLine();
        graph.writeService(service, "rectangle");
        graph.close();

        request.setHandled(true);
        response.setStatus(200);
    }

    private void fanOut(Service service, Graph graph, List<Service> services, List<String> foundIds) throws IOException {
        List<ServiceRef> serviceRefs;
        serviceRefs = dataAccess.getServiceRefsByClient(service.getServiceId());
        if (serviceRefs != null && !serviceRefs.isEmpty()) {
            for (ServiceRef serviceRef : serviceRefs) {
                if (!foundIds.contains(serviceRef.getServerServiceId())) {
                    Service temp = dataAccess.getService(serviceRef.getServerServiceId());
                    graph.writeLink(service.getServiceAbbr(), temp.getServiceAbbr());
                    graph.writeService(temp, "ellipse");
                    services.add(temp);
                    foundIds.add(temp.getServiceId());
                }
            }
        }
    }

}
