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
import com.northernwall.hadrian.domain.Team;
import java.io.IOException;
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
public class GraphAllHandler extends AbstractHandler {

    private final DataAccess dataAccess;

    public GraphAllHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        List<Team> teams;
        List<Service> services;
        List<ServiceRef> serviceRefs;

        Graph graph = new Graph(response, true);

        teams = dataAccess.getTeams();
        if (teams != null && !teams.isEmpty()) {
            int c = 0;
            for (Team team : teams) {
                services = dataAccess.getServices(team.getTeamId());
                if (services != null && !services.isEmpty()) {
                    graph.startSubGraph(c);
                    for (Service service : services) {
                        graph.writeService(service, "ellipse");
                    }
                    graph.finishSubGraph(team.getTeamName());
                }
                c++;
            }
            for (Team team : teams) {
                services = dataAccess.getServices(team.getTeamId());
                if (services != null && !services.isEmpty()) {
                    for (Service service : services) {
                        serviceRefs = dataAccess.getServiceRefsByClient(service.getServiceId());
                        if (serviceRefs != null && !serviceRefs.isEmpty()) {
                            for (ServiceRef serviceRef : serviceRefs) {
                                Service temp = dataAccess.getService(serviceRef.getServerServiceId());
                                graph.writeLink(service.getServiceAbbr(), temp.getServiceAbbr());
                            }
                        }
                    }
                    graph.newLine();
                }
            }
        }
        graph.close();

        request.setHandled(true);
        response.setStatus(200);
    }

}
