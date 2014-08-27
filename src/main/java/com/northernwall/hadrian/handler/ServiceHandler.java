package com.northernwall.hadrian.handler;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceHeader;
import com.northernwall.hadrian.domain.Version;
import com.northernwall.hadrian.formData.ServiceFormData;
import java.io.IOException;
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

public class ServiceHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(ServiceHandler.class);

    private final DataAccess dataAccess;
    private final Gson gson;

    public ServiceHandler(DataAccess dataAccess, Gson gson) {
        this.dataAccess = dataAccess;
        this.gson = gson;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.equals("/services/services.json")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "GET":
                        listServices(response);
                        break;
                    case "POST":
                        createService(request);
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
            } else if (target.matches("/services/\\w+.json")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "GET":
                        getService(response, target.substring(10, target.length() - 5));
                        break;
                    case "POST":
                        updateService(request);
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
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

    private void createService(Request request) throws IOException {
        ServiceFormData serviceData = gson.fromJson(new InputStreamReader(request.getInputStream()), ServiceFormData.class);
        Service cur = dataAccess.getService(serviceData._id);

        if (cur != null) {
            return;
        }
        Service service = new Service();
        service.setId(serviceData._id);
        service.name = serviceData.name;
        service.team = serviceData.team;
        service.description = serviceData.description;
        service.imageLogo = "/ui/img/serviceLogo.png";
        service.images = new LinkedList<>();
        service.images.add(0, "/ui/img/serviceLogo.png");
        service.access = serviceData.access;
        service.type = serviceData.type;
        service.state = serviceData.state;
        service.busImportance = serviceData.busImportance;
        service.pii = serviceData.pii;
        Version version = new Version();
        version.api = serviceData.api;
        version.impl = serviceData.impl;
        version.status = serviceData.status;
        service.versions = new LinkedList<>();
        service.versions.add(version);
        dataAccess.save(service);
    }

    private void updateService(Request request) throws IOException {
        ServiceFormData serviceData = gson.fromJson(new InputStreamReader(request.getInputStream()), ServiceFormData.class);
        Service cur = dataAccess.getService(serviceData._id);

        if (cur == null) {
            return;
        }
        cur.name = serviceData.name;
        cur.team = serviceData.team;
        cur.description = serviceData.description;
        cur.access = serviceData.access;
        cur.type = serviceData.type;
        cur.state = serviceData.state;
        dataAccess.update(cur);
    }

}
