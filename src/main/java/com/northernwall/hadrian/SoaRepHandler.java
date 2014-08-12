package com.northernwall.hadrian;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.domain.Color;
import com.northernwall.hadrian.domain.Edge;
import com.northernwall.hadrian.domain.Network;
import com.northernwall.hadrian.domain.Node;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceHeader;
import com.northernwall.hadrian.domain.ServiceRef;
import com.northernwall.hadrian.domain.Version;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

public class SoaRepHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(SoaRepHandler.class);

    private final SoaRepDataAccess dataAccess;
    private final Gson gson;

    public SoaRepHandler(SoaRepDataAccess dataAccess, Gson gson) {
        this.dataAccess = dataAccess;
        this.gson = gson;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        logger.info("Handling {} request {}", request.getMethod(), target);
        try {
            if (target.equals("/")) {
                redirect(response);
            } else if (target.equals("/availablity")) {
            } else if (target.equals("/services/services.json")) {
                switch (request.getMethod()) {
                    case "GET":
                        listServices(response);
                        break;
                    case "POST":
                        createService(request, response);
                        break;
                }
            } else if (target.startsWith("/services/") && target.endsWith(".json")) {
                getService(response, target.substring(10, target.length() - 5));
            } else if (target.startsWith("/servicesGraph.json")) {
                getServicesGraph(response);
            } else if (target.equals("/ui/")) {
                getContent(response, "/webapp/index.html");
            } else if (target.startsWith("/ui/")) {
                getContent(response, "/webapp" + target.substring(3));
            }
            response.setStatus(200);
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
        request.setHandled(true);
    }

    private void redirect(HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        response.getOutputStream().print("<html><head><meta http-equiv=\"refresh\" content=\"1;url=/ui/\"></head><body></body></html>");
        response.setStatus(200);
    }

    private void listServices(HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        List<ServiceHeader> services = dataAccess.getServiceHeaders();
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            jw.beginArray();
            for (ServiceHeader service : services) {
                gson.toJson(service, ServiceHeader.class, jw);
            }
            jw.endArray();
        }
        response.setStatus(200);
    }

    private void getService(HttpServletResponse response, String id) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        Service service = dataAccess.getService(id);
        if (service == null) {
            throw new RuntimeException("Could not find service with id '" + id + "'");
        }
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(service, Service.class, jw);
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
                                    edge.color = "blue";
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

    private void getContent(HttpServletResponse response, String resource) throws IOException {
        byte[] buffer = new byte[1024];
        try (InputStream is = this.getClass().getResourceAsStream(resource)) {
            if (is == null) {
                throw new RuntimeException("Can not find resource '" + resource + "'");
            }
            int len = is.read(buffer);
            while (len != -1) {
                response.getOutputStream().write(buffer, 0, len);
                len = is.read(buffer);
            }
        }
        response.setStatus(200);
    }

    private void createService(Request request, HttpServletResponse response) throws IOException {
        Service service = gson.fromJson(new InputStreamReader(request.getInputStream()), Service.class);
        Service cur = dataAccess.getService(service.getId());

        if (cur == null) {
            Version version = new Version();
            version.api = "1";
            version.impl = "0";
            version.status = "Proposed";
            service.versions = new LinkedList<>();
            service.versions.add(version);
            dataAccess.save(service);
        } else {
            cur.name = service.name;
            cur.team = service.team;
            cur.description = service.description;
            cur.access = service.access;
            cur.type = service.type;
            cur.state = service.state;
            dataAccess.update(cur);
        }
    }

}
