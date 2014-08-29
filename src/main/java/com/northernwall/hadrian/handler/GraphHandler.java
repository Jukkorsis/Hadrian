package com.northernwall.hadrian.handler;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Network;
import com.northernwall.hadrian.domain.ServiceRefView;
import com.northernwall.hadrian.domain.VersionView;
import com.northernwall.hadrian.formData.Color;
import com.northernwall.hadrian.formData.Edge;
import com.northernwall.hadrian.formData.Node;
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

    private final DataAccess dataAccess;
    private final Gson gson;

    public GraphHandler(DataAccess dataAccess, Gson gson) {
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
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            List<VersionView> versions = dataAccess.getVersionVeiw();
            if (versions != null && !versions.isEmpty()) {
                for (VersionView version : versions) {
                    Node node = new Node();
                    node.id = indexes.size();
                    node.label = version.serviceId + "-v" + version.versionId;
                    if (version.type != null && version.type.equals("Application")) {
                        node.shape = "box";
                    } else {
                        node.shape = "ellipse";
                    }
                    node.title = buildNodeTitle(version);
                    node.color = new Color();
                    if (version.access != null && version.access.equals("Internal")) {
                        node.color.background = "white";
                    } else {
                        node.color.background = "yellow";
                    }
                    if (version.status != null && (version.status.equals("Retiring") || version.status.equals("Retired"))) {
                        node.color.border = "red";
                        node.borderWidth = 3;
                    } else {
                        node.color.border = "black";
                        node.borderWidth = 1;
                    }
                    indexes.add(version.serviceId + "-v" + version.versionId);
                    network.nodes.add(node);
                }
            }
            List<ServiceRefView> refs = dataAccess.getServiceRefVeiw();
            if (refs != null && !refs.isEmpty()) {
                for (ServiceRefView ref : refs) {
                    int i = indexes.indexOf(ref.serviceId + "-v" + ref.versionId);
                    int ii = indexes.indexOf(ref.refServiceId + "-v" + ref.refVersionId);
                    if (i >= 0 && i >= 0) {
                        Edge edge = new Edge();
                        edge.from = i;
                        edge.to = ii;
                        edge.style = "arrow";
                        switch (ref.scope) {
                            case "sync":
                                edge.color = "blue";
                                edge.width = 2;
                                break;
                            case "async":
                                edge.color = "black";
                                edge.width = 1;
                                break;
                            case "indirect":
                                edge.color = "brown";
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
            gson.toJson(network, Network.class, jw);
        }
        response.setStatus(200);
    }

    private String buildNodeTitle(VersionView version) {
        StringBuilder str = new StringBuilder();
        str.append("<a href='#/services/");
        str.append(version.serviceId);
        str.append("'>");
        str.append(version.serviceId);
        str.append(" - ");
        str.append(version.name);
        str.append("</a><br/>");
        str.append(version.team);
        str.append("<br/>");
        str.append(version.access);
        str.append(" ");
        str.append(version.type);
        str.append("<br/>");
        switch (version.status) {
            case "Proposed":
                str.append("Proposed, ");
                str.append(version.serviceId);
                str.append(" v");
                str.append(version.versionId);
                str.append(" is not yet live.");
                break;
            case "Active":
                str.append("Live and actively maintained by ");
                str.append(version.team);
                break;
            case "Live":
                str.append("Live but not maintained");
                break;
            case "Retiring":
                str.append(version.team);
                str.append(" is actively retiring v");
                str.append(version.versionId);
                str.append(" of ");
                str.append(version.serviceId);
                break;
            case "Retired":
                str.append("v");
                str.append(version.versionId);
                str.append(" of ");
                str.append(version.serviceId);
                str.append(" has been retired");
                break;
        }
        return str.toString();
    }

}
