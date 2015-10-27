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
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceRef;
import com.northernwall.hadrian.graph.dao.Color;
import com.northernwall.hadrian.graph.dao.Edge;
import com.northernwall.hadrian.graph.dao.Network;
import com.northernwall.hadrian.graph.dao.Node;
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
            if (target.startsWith("/v1/graph")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                getServicesGraph(response);
                request.setHandled(true);
                response.setStatus(200);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void getServicesGraph(HttpServletResponse response) throws IOException {
        Network network = new Network();
        List<String> indexes = new LinkedList<>();
        response.setContentType(Const.JSON);
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            List<Service> services = dataAccess.getServices();
            if (services != null && !services.isEmpty()) {
                for (Service service : services) {
                    Node node = new Node();
                    node.id = indexes.size();
                    node.label = buildNodeTitle(service);
                    node.shape = "box"; //"ellipse";
                    node.title = buildNodeTitle(service);
                    node.color = new Color();
                    node.color.background = "white";
                    node.color.border = "red";
                    node.borderWidth = 2;
                    node.mass = 1;
                    indexes.add(service.getServiceId());
                    network.getNodes().add(node);
                }
            }
            List<ServiceRef> refs = dataAccess.getServiceRefs();
            if (refs != null && !refs.isEmpty()) {
                for (ServiceRef ref : refs) {
                    int i = indexes.indexOf(ref.getClientServiceId());
                    int ii = indexes.indexOf(ref.getServerServiceId());
                    network.getNodes().get(ii).mass++;
                    if (i >= 0 && ii >= 0) {
                        Edge edge = new Edge();
                        edge.from = i;
                        edge.to = ii;
                        edge.color = "blue";
                        edge.width = 2;
                        network.getEdges().add(edge);
                    }
                }
            }
            gson.toJson(network, Network.class, jw);
        }
        response.setStatus(200);
    }

    private String buildNodeTitle(Service service) {
        return service.getServiceName().replace("  ", " ").replace(" ", "\n");
    }

}
