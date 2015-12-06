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

import com.google.gson.Gson;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceRef;
import com.northernwall.hadrian.domain.Team;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
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
public class GraphHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(GraphHandler.class);

    private final DataAccess dataAccess;
    private final Gson gson;

    public GraphHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        this.gson = new Gson();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (request.getMethod().equals(Const.HTTP_GET) && target.startsWith("/v1/graph/")) {
                if (target.equals("/v1/graph/all")) {
                    logger.info("Handling {} request {}", request.getMethod(), target);
                    produceAllGraph(response);
                    request.setHandled(true);
                    response.setStatus(200);
                } else if (target.matches("/v1/graph/fanout/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                    logger.info("Handling {} request {}", request.getMethod(), target);
                    String id = target.substring(17);
                    produceFanOutGraph(response, id);
                    request.setHandled(true);
                    response.setStatus(200);
                } else if (target.matches("/v1/graph/fanin/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                    logger.info("Handling {} request {}", request.getMethod(), target);
                    String id = target.substring(16);
                    produceFanInGraph(response, id);
                    request.setHandled(true);
                    response.setStatus(200);
                }
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void produceAllGraph(HttpServletResponse response) throws IOException {
        List<Team> teams;
        List<Service> services;
        List<ServiceRef> serviceRefs;

        response.setContentType(Const.TEXT);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));

        writer.append("digraph G {");
        writer.newLine();
        teams = dataAccess.getTeams();
        if (teams != null && !teams.isEmpty()) {
            int c = 0;
            for (Team team : teams) {
                writer.append(" subgraph cluster_" + c + " {");
                writer.newLine();
                writer.append("  color=blue;");
                writer.newLine();
                writer.append("  node [style=filled];");
                writer.newLine();
                services = dataAccess.getServices(team.getTeamId());
                if (services != null && !services.isEmpty()) {
                    for (Service service : services) {
                        writer.append("  " + service.getServiceAbbr() + ";");
                        writer.newLine();
                    }
                }
                writer.append("  label = \"" + team.getTeamName() + "\";");
                writer.newLine();
                writer.append(" }");
                writer.newLine();
                writer.newLine();
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
                                writer.append(" " + service.getServiceAbbr() + " -> " + temp.getServiceAbbr() + ";");
                                writer.newLine();
                            }
                        }
                    }
                    writer.newLine();
                }
            }
        }
        writer.append("}");
        writer.flush();
    }

    private void produceFanInGraph(HttpServletResponse response, String serviceId) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
        writer.append("digraph G {");
        writer.newLine();
        List<Service> services = new LinkedList<>();
        List<String> foundIds = new LinkedList<>();
        Service service = dataAccess.getService(serviceId);
        services.add(service);
        foundIds.add(service.getServiceId());
        while (!services.isEmpty()) {
            fanIn(services.remove(0), writer, services, foundIds);
        }
        writer.newLine();
        writer.append(service.getServiceAbbr() + " [shape=square];");
        writer.append("}");
        writer.flush();
    }

    private void fanIn(Service service, BufferedWriter writer, List<Service> services, List<String> foundIds) throws IOException {
        List<ServiceRef> serviceRefs;
        serviceRefs = dataAccess.getServiceRefsByServer(service.getServiceId());
        if (serviceRefs != null && !serviceRefs.isEmpty()) {
            for (ServiceRef serviceRef : serviceRefs) {
                if (!foundIds.contains(serviceRef.getClientServiceId())) {
                    Service temp = dataAccess.getService(serviceRef.getClientServiceId());
                    writer.append(" " + temp.getServiceAbbr() + " -> " + service.getServiceAbbr() + ";");
                    writer.newLine();
                    services.add(temp);
                    foundIds.add(temp.getServiceId());
                }
            }
        }
    }

    private void produceFanOutGraph(HttpServletResponse response, String serviceId) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
        writer.append("digraph G {");
        writer.newLine();
        List<Service> services = new LinkedList<>();
        List<String> foundIds = new LinkedList<>();
        Service service = dataAccess.getService(serviceId);
        services.add(service);
        foundIds.add(service.getServiceId());
        while (!services.isEmpty()) {
            fanOut(services.remove(0), writer, services, foundIds);
        }
        writer.newLine();
        writer.append(service.getServiceAbbr() + " [shape=square];");
        writer.append("}");
        writer.flush();
    }

    private void fanOut(Service service, BufferedWriter writer, List<Service> services, List<String> foundIds) throws IOException {
        List<ServiceRef> serviceRefs;
        serviceRefs = dataAccess.getServiceRefsByClient(service.getServiceId());
        if (serviceRefs != null && !serviceRefs.isEmpty()) {
            for (ServiceRef serviceRef : serviceRefs) {
                if (!foundIds.contains(serviceRef.getServerServiceId())) {
                    Service temp = dataAccess.getService(serviceRef.getServerServiceId());
                    writer.append(" " + service.getServiceAbbr() + " -> " + temp.getServiceAbbr() + ";");
                    writer.newLine();
                    services.add(temp);
                    foundIds.add(temp.getServiceId());
                }
            }
        }
    }

}
