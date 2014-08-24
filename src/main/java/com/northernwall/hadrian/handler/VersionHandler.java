package com.northernwall.hadrian.handler;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.SoaRepDataAccess;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceRef;
import com.northernwall.hadrian.domain.Version;
import com.northernwall.hadrian.domain.VersionHeader;
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

public class VersionHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(VersionHandler.class);

    private final SoaRepDataAccess dataAccess;
    private final Gson gson;

    public VersionHandler(SoaRepDataAccess dataAccess, Gson gson) {
        this.dataAccess = dataAccess;
        this.gson = gson;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.matches("/services/\\w+/versions.json")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "GET":
                        break;
                    case "POST":
                        createVersion(request);
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
                        updateVersion(request);
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

    private void createVersion(Request request) throws IOException {
        VersionFormData versionData = gson.fromJson(new InputStreamReader(request.getInputStream()), VersionFormData.class);
        Service cur = dataAccess.getService(versionData._id);

        if (cur == null) {
            return;
        }

        if (cur.findVersion(versionData.api) != null) {
            return;
        }
        Version version = new Version();
        version.api = versionData.api;
        version.impl = versionData.impl;
        version.status = versionData.status;
        cur.addVersion(version);
        dataAccess.update(cur);
    }

    private void updateVersion(Request request) throws IOException {
        InputStreamReader isr = new InputStreamReader(request.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        String s = br.readLine();
        System.out.println(s);
        VersionFormData versionData = gson.fromJson(s, VersionFormData.class);
        Service cur = dataAccess.getService(versionData._id);

        if (cur == null) {
            return;
        }
        Version version = cur.findVersion(versionData.api);
        if (version == null) {
            return;
        }
        version.impl = versionData.impl;
        if (!version.status.equals(versionData.status)) {
            version.status = versionData.status;
            generateWarningsForUsedBy(cur, version);
        }
        versionData.uses1.addAll(versionData.uses2);
        for (UsesFormData usesData : versionData.uses1) {
            ServiceRef serviceRef = version.findUses(usesData.serviceId, usesData.versionId);
            if (serviceRef != null) {
                if (!usesData.scope.equals(serviceRef.scope)) {
                    if (usesData.scope.equals("none")) {
                        version.uses.remove(serviceRef);
                        removeUsedBy(usesData.serviceId, usesData.versionId, cur.getId(), version.api);
                    } else {
                        serviceRef.scope = usesData.scope;
                        updateUsedBy(usesData.serviceId, usesData.versionId, cur.getId(), version.api, usesData.scope);
                    }
                }
            } else if (!usesData.scope.equals("none")) {
                serviceRef = new ServiceRef();
                serviceRef.service = usesData.serviceId;
                serviceRef.version = usesData.versionId;
                serviceRef.scope = usesData.scope;
                generateWarningsForUsesRef(serviceRef);
                if (version.uses == null) {
                    version.uses = new LinkedList<>();
                }
                version.uses.add(serviceRef);
                addUsedBy(usesData.serviceId, usesData.versionId, cur.getId(), version.api, usesData.scope);
            }
        }
        dataAccess.update(cur);
    }

    private void addUsedBy(String serviceId, String versionId, String refServiceId, String refVersionId, String scope) {
        Service cur = dataAccess.getService(serviceId);
        if (cur == null) {
            return;
        }

        Version version = cur.findVersion(versionId);
        if (version == null) {
            logger.error("Could not find version {} in service {}", versionId, serviceId);
            return;
        }

        ServiceRef serviceRef = new ServiceRef();
        serviceRef.service = refServiceId;
        serviceRef.version = refVersionId;
        serviceRef.scope = scope;

        version.addUsedBy(serviceRef);
        dataAccess.update(cur);
    }

    private void updateUsedBy(String serviceId, String versionId, String refServiceId, String refVersionId, String scope) {
        Service cur = dataAccess.getService(serviceId);
        if (cur == null) {
            return;
        }

        Version version = cur.findVersion(versionId);
        if (version == null) {
            logger.error("Could not find version {} in service {}", versionId, serviceId);
            return;
        }

        ServiceRef serviceRef = version.findUsedBy(refServiceId, refVersionId);
        if (serviceRef == null) {
            logger.error("Could not find usedby serviceRef on version {} in service {}", versionId, serviceId);
            return;
        }
        serviceRef.scope = scope;
        dataAccess.update(cur);
    }

    private void removeUsedBy(String serviceId, String versionId, String refServiceId, String refVersionId) {
        Service cur = dataAccess.getService(serviceId);
        if (cur == null) {
            return;
        }

        Version version = cur.findVersion(versionId);
        if (version == null) {
            logger.error("Could not find version {} in service {}", versionId, serviceId);
            return;
        }

        ServiceRef serviceRef = version.findUsedBy(refServiceId, refVersionId);
        if (serviceRef == null) {
            logger.error("Could not find usedby serviceRef on version {} in service {}", versionId, serviceId);
            return;
        }
        version.usedby.remove(serviceRef);
        dataAccess.update(cur);
    }

    private void generateWarningsForUsesRef(ServiceRef serviceRef) {
        Service usedByService = dataAccess.getService(serviceRef.service);

        if (usedByService == null) {
            logger.error("Could not find service {}", serviceRef.service);
            return;
        }

        for (Version usedByVersion : usedByService.versions) {
            if (usedByVersion.api.equals(serviceRef.version)) {
                serviceRef.retireWarnings = (usedByVersion.status.equals("Retired") || usedByVersion.status.equals("Retiring"));
            }
        }
    }

    private void generateWarningsForUsedBy(Service service, Version version) {
        if (version.usedby != null && !version.usedby.isEmpty()) {
            for (ServiceRef usedByServiceRef : version.usedby) {
                Service usedByService = dataAccess.getService(usedByServiceRef.service);
                if (usedByService != null) {
                    Version usedByVersion = usedByService.findVersion(usedByServiceRef.version);
                    if (usedByVersion != null) {
                        ServiceRef serviceRef = usedByVersion.findUses(service.getId(), version.api);
                        if (serviceRef != null) {
                            serviceRef.retireWarnings = (version.status.equals("Retired") || version.status.equals("Retiring"));
                            dataAccess.update(usedByService);
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
                        gson.toJson(versionHeader, VersionHeader.class, jw);
                    }
                }
            }
            jw.endArray();
        }
    }

}
