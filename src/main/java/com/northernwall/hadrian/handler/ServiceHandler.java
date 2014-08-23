package com.northernwall.hadrian.handler;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.SoaRepDataAccess;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceHeader;
import com.northernwall.hadrian.domain.ServiceRef;
import com.northernwall.hadrian.domain.Version;
import com.northernwall.hadrian.domain.VersionHeader;
import com.northernwall.hadrian.formData.ServiceFormData;
import com.northernwall.hadrian.formData.UsesFormData;
import com.northernwall.hadrian.formData.VersionFormData;
import java.io.BufferedReader;
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

    private final SoaRepDataAccess dataAccess;
    private final Gson gson;

    public ServiceHandler(SoaRepDataAccess dataAccess, Gson gson) {
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
                        createService(request, response);
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
                        updateService(request, response);
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
            } else if (target.matches("/services/\\w+/versions.json")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "GET":
                        break;
                    case "POST":
                        createVersion(request, response);
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
            } else if (target.matches("/services/\\w+/\\w.json")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "GET":
                        break;
                    case "POST":
                        updateVersion(request, response);
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
            } else if (target.matches("/services/\\w+/\\w/uses.json")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "GET":
                        String temp = target.substring(10, target.length() - 10);
                        int i = temp.indexOf("/");
                        getVersionUses(response, temp.substring(0, i), temp.substring(i + 1));
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

    private void createService(Request request, HttpServletResponse response) throws IOException {
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
        service.access = serviceData.access;
        service.type = serviceData.type;
        service.state = serviceData.state;
        Version version = new Version();
        version.api = serviceData.api;
        version.impl = serviceData.impl;
        version.status = serviceData.status;
        service.versions = new LinkedList<>();
        service.versions.add(version);
        dataAccess.save(service);
    }

    private void updateService(Request request, HttpServletResponse response) throws IOException {
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

    private void createVersion(Request request, HttpServletResponse response) throws IOException {
        VersionFormData versionData = gson.fromJson(new InputStreamReader(request.getInputStream()), VersionFormData.class);
        Service cur = dataAccess.getService(versionData._id);

        if (cur == null) {
            return;
        }
        if (cur.versions == null) {
            cur.versions = new LinkedList<>();
        } else if (!cur.versions.isEmpty()) {
            for (Version version : cur.versions) {
                if (version.api.equals(versionData.api)) {
                    return;
                }
            }
        }
        Version version = new Version();
        version.api = versionData.api;
        version.impl = versionData.impl;
        version.status = versionData.status;
        cur.versions.add(version);
        dataAccess.update(cur);
    }

    private void updateVersion(Request request, HttpServletResponse response) throws IOException {
        InputStreamReader isr = new InputStreamReader(request.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        String s = br.readLine();
        System.out.println(s);
        VersionFormData versionData = gson.fromJson(s, VersionFormData.class);
        Service cur = dataAccess.getService(versionData._id);

        if (cur == null) {
            return;
        }
        for (Version version : cur.versions) {
            if (version.api.equals(versionData.api)) {
                version.impl = versionData.impl;
                version.status = versionData.status;
                versionData.uses1.addAll(versionData.uses2);
                for (UsesFormData usesData : versionData.uses1) {
                    boolean found = false;
                    if (version.uses != null && !version.uses.isEmpty()) {
                        List<ServiceRef> oldRefs = null;
                        for (ServiceRef serviceRef : version.uses) {
                            if (usesData.serviceId.equals(serviceRef.service) && usesData.versionId.equals(serviceRef.version)) {
                                found = true;
                                if (!usesData.scope.equals(serviceRef.scope)) {
                                    if (usesData.scope.equals("none")) {
                                        if (oldRefs == null) {
                                            oldRefs = new LinkedList<>();
                                        }
                                        oldRefs.add(serviceRef);
                                    } else {
                                        serviceRef.scope = usesData.scope;
                                        //todo: calc warnings
                                        updateUsedBy(usesData.serviceId, usesData.versionId, cur.getId(), version.api, usesData.scope);
                                    }
                                }
                            }
                        }
                        if (oldRefs != null) {
                            for (ServiceRef serviceRef : oldRefs) {
                                version.uses.remove(serviceRef);
                            }
                        }
                    }
                    if (!found && !usesData.scope.equals("none")) {
                        ServiceRef serviceRef = new ServiceRef();
                        serviceRef.service = usesData.serviceId;
                        serviceRef.version = usesData.versionId;
                        serviceRef.scope = usesData.scope;
                        //todo: calc warnings
                        if (version.uses == null) {
                            version.uses = new LinkedList<>();
                        }
                        version.uses.add(serviceRef);
                        addUsedBy(usesData.serviceId, usesData.versionId, cur.getId(), version.api, usesData.scope);
                    }
                }
                dataAccess.update(cur);
                return;
            }
        }
    }

    private void addUsedBy(String serviceId, String versionId, String refServiceId, String refVersionId, String scope) {
        Service cur = dataAccess.getService(serviceId);

        if (cur == null) {
            return;
        }

        ServiceRef serviceRef = new ServiceRef();
        serviceRef.service = refServiceId;
        serviceRef.version = refVersionId;
        serviceRef.scope = scope;

        for (Version version : cur.versions) {
            if (version.api.equals(versionId)) {
                version.usedby.add(serviceRef);
                dataAccess.update(cur);
                return;
            }
        }
    }

    private void updateUsedBy(String serviceId, String versionId, String refServiceId, String refVersionId, String scope) {
        Service cur = dataAccess.getService(serviceId);

        if (cur == null) {
            return;
        }

        for (Version version : cur.versions) {
            if (version.api.equals(versionId)) {
                if (version.usedby != null && !version.usedby.isEmpty()) {
                    for (ServiceRef serviceRef : version.usedby) {
                        if (serviceRef.service.equals(refServiceId) && serviceRef.version.equals(refVersionId)) {
                            serviceRef.scope = scope;
                            dataAccess.update(cur);
                            return;
                        }
                    }
                }
            }
        }
    }

    private void getVersionUses(HttpServletResponse response, String serviceId, String versionId) throws IOException {
        logger.info("serviceId {} versionId {}", serviceId, versionId);
        Service service = dataAccess.getService(serviceId);
        Version version = null;
        if (service.versions == null || service.versions.isEmpty()) {
            return;
        }
        for (Version temp : service.versions) {
            if (temp.api.equals(versionId)) {
                version = temp;
            }
        }
        if (version == null) {
            return;
        }
        List<VersionHeader> versionHeaders = dataAccess.getVersions();
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            jw.beginArray();
            if (versionHeaders != null && !versionHeaders.isEmpty()) {
                for (VersionHeader versionHeader : versionHeaders) {
                    if (!versionHeader.serviceId.equals(serviceId)) {
                        if (version.uses != null && !version.uses.isEmpty()) {
                            for (ServiceRef ref : version.uses) {
                                if (ref.service.equals(versionHeader.serviceId) && ref.version.equals(versionHeader.versionId)) {
                                    versionHeader.scope = ref.scope;
                                }
                            }
                        }
                        //TODO: check status of version, don't include versions that are retiring or retired and there is no existing link
                        logger.info("serviceId {} versionId {}", versionHeader.serviceId, versionHeader.versionId);
                        gson.toJson(versionHeader, VersionHeader.class, jw);
                    }
                }
            }
            jw.endArray();
        }
    }

}
