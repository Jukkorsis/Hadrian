package com.northernwall.hadrian.handler;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.SoaRepDataAccess;
import com.northernwall.hadrian.domain.Color;
import com.northernwall.hadrian.domain.Edge;
import com.northernwall.hadrian.domain.Network;
import com.northernwall.hadrian.domain.Node;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceRef;
import com.northernwall.hadrian.domain.Version;
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

public class GraphHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(GraphHandler.class);

    private final SoaRepDataAccess dataAccess;
    private final Gson gson;

    public GraphHandler(SoaRepDataAccess dataAccess, Gson gson) {
        this.dataAccess = dataAccess;
        this.gson = gson;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.startsWith("/servicesGraph.json")) {
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
        response.setContentType("application/json;charset=utf-8");
        List<Service> services = dataAccess.getServices();
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            if (services != null) {
                for (Service service : services) {
                    if (service.versions != null) {
                        for (Version version : service.versions) {
                            Node node = new Node();
                            node.id = indexes.size();
                            node.label = service.getId() + "-v" + version.api;
                            if (service.type != null && service.type.equals("Application")) {
                                node.shape = "box";
                            } else {
                                node.shape = "ellipse";
                            }
                            node.title = buildNodeTitle(service, version);
                            node.color = new Color();
                            if (service.access != null && service.access.equals("Internal")) {
                                node.color.background = "white";
                            } else {
                                node.color.background = "yellow";
                            }
                            if (version.status != null && (version.status.equals("Deprecating") || version.status.equals("Deprecated"))) {
                                node.color.border = "red";
                                node.borderWidth = 3;
                            } else {
                                node.color.border = "black";
                                node.borderWidth = 1;
                            }
                            indexes.add(service.getId() + "-v" + version.api);
                            network.nodes.add(node);
                        }
                    }
                }
                for (Service service : services) {
                    if (service.versions != null) {
                        for (Version version : service.versions) {
                            int i = indexes.indexOf(service.getId() + "-v" + version.api);
                            if (version.uses != null) {
                                for (ServiceRef ref : version.uses) {
                                    Edge edge = new Edge();
                                    edge.from = i;
                                    edge.to = indexes.indexOf(ref.service + "-v" + ref.version);
                                    edge.style = "arrow";
                                    if (ref.warnings != null && !ref.warnings.isEmpty()) {
                                        edge.label = "Warning";
                                        edge.title = ref.warnings;
                                    }
                                    switch (ref.scope) {
                                        case "sync":
                                            edge.color = "blue";
                                            edge.width = 2;
                                            break;
                                        case "async":
                                            edge.color = "black";
                                            edge.width = 1;
                                            break;
                                        case "support":
                                            edge.color = "green";
                                            edge.width = 1;
                                            break;
                                    }
                                    network.edges.add(edge);
                                }
                            }
                        }
                    }
                }
            }
            gson.toJson(network, Network.class, jw);
        }
        response.setStatus(200);
    }

    private String buildNodeTitle(Service service, Version version) {
        StringBuilder str = new StringBuilder();
        str.append("<a href='#/services/");
        str.append(service.getId());
        str.append("'>");
        str.append(service.getId());
        str.append(" - ");
        str.append(service.name);
        str.append("</a><br/>");
        str.append(service.team);
        str.append("<br/>");
        str.append(service.access);
        str.append(" ");
        str.append(service.type);
        str.append("<br/>");
        switch (version.status) {
            case "Proposed":
                str.append("Proposed, ");
                str.append(service.getId());
                str.append(" is not yet live");
                break;
            case "Active":
                str.append("Live and actively maintained");
                break;
            case "Live":
                str.append("Live but not maintained");
                break;
            case "Retiring":
                str.append(service.team);
                str.append(" is actively retiring v");
                str.append(version.api);
                break;
            case "Retired":
                str.append(version.api);
                str.append(" of ");
                str.append(service.getId());
                str.append(" has been retired");
                break;
        }
        return str.toString();
    }

}
