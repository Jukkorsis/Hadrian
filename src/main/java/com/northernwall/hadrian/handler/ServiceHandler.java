package com.northernwall.hadrian.handler;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.WarningProcessor;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.ConfigItem;
import com.northernwall.hadrian.domain.DataCenter;
import com.northernwall.hadrian.domain.Endpoint;
import com.northernwall.hadrian.domain.HaDimension;
import com.northernwall.hadrian.domain.HaRating;
import com.northernwall.hadrian.domain.Link;
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
    private final WarningProcessor warningProcessor;

    public ServiceHandler(DataAccess dataAccess, Gson gson, WarningProcessor warningProcessor) {
        this.dataAccess = dataAccess;
        this.gson = gson;
        this.warningProcessor = warningProcessor;
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
        for (ConfigItem item : dataAccess.getConfig().dataCenters) {
            boolean found = false;
            for (DataCenter dataCenter : service.dataCenters) {
                if (dataCenter.name.equals(item.code)) {
                    found = true;
                }
            }
            if (!found) {
                DataCenter dc = new DataCenter();
                dc.name = item.code;
                dc.status = "None";
                service.dataCenters.add(dc);
            }
        }
        for (HaDimension haDimension : dataAccess.getConfig().haDimensions) {
            boolean found = false;
            for (HaRating haRating : service.haRatings) {
                if (haRating.name.equals(haDimension.name)) {
                    found = true;
                    haRating.levels = new LinkedList<>();
                    for (ConfigItem item : haDimension.levels) {
                        haRating.levels.add(item.code);
                    }
                }
            }
            if (!found) {
                HaRating haRating = new HaRating();
                haRating.name = haDimension.name;
                haRating.level = haDimension.levels.get(haDimension.levels.size() - 1).code;
                haRating.levels = new LinkedList<>();
                for (ConfigItem item : haDimension.levels) {
                    haRating.levels.add(item.code);
                }
                service.haRatings.add(haRating);
            }
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
        service.state = serviceData.state;
        service.access = serviceData.access;
        service.type = serviceData.type;
        service.tech = serviceData.tech;
        service.busValue = serviceData.busValue;
        service.pii = serviceData.pii;
        Version version = new Version();
        version.api = serviceData.api;
        version.impl = serviceData.impl;
        version.status = serviceData.status;
        service.versions = new LinkedList<>();
        service.versions.add(version);
        for (HaDimension haDimension : dataAccess.getConfig().haDimensions) {
            HaRating haRating = new HaRating();
            haRating.name = haDimension.name;
            haRating.level = haDimension.levels.get(haDimension.levels.size() - 1).code;
            service.haRatings.add(haRating);
        }
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
        cur.state = serviceData.state;
        cur.access = serviceData.access;
        cur.type = serviceData.type;
        cur.tech = serviceData.tech;
        cur.busValue = serviceData.busValue;
        cur.pii = serviceData.pii;
        cur.endpoints = new LinkedList<>();
        for (Endpoint endpoint : serviceData.endpoints) {
            if (endpoint.env != null && !endpoint.env.isEmpty() && endpoint.url != null && !endpoint.url.isEmpty()) {
                cur.endpoints.add(endpoint);
            }
        }
        cur.links = new LinkedList<>();
        for (Link link : serviceData.links) {
            if (link.name != null && !link.name.isEmpty() && link.url != null && !link.url.isEmpty()) {
                cur.links.add(link);
            }
        }
        cur.dataCenters = new LinkedList<>();
        for (DataCenter dataCenter : serviceData.dataCenters) {
            if (dataCenter.status != null && !dataCenter.status.equals("None")) {
                cur.dataCenters.add(dataCenter);
            }
        }
        cur.haRatings = serviceData.haRatings;
        dataAccess.update(cur);

        warningProcessor.scanServices();
    }

}
