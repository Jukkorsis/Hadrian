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

package com.northernwall.hadrian.handler;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.WarningProcessor;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Link;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceRef;
import com.northernwall.hadrian.domain.Version;
import com.northernwall.hadrian.domain.VersionView;
import com.northernwall.hadrian.formData.UsesFormData;
import com.northernwall.hadrian.formData.VersionFormData;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Comparator;
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
public class VersionHandler extends AbstractHandler {
    private final static Logger logger = LoggerFactory.getLogger(VersionHandler.class);

    private final DataAccess dataAccess;
    private final Gson gson;
    private final WarningProcessor warningProcessor;

    public VersionHandler(DataAccess dataAccess, Gson gson, WarningProcessor warningProcessor) {
        this.dataAccess = dataAccess;
        this.gson = gson;
        this.warningProcessor = warningProcessor;
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
            } else if (target.matches("/services/\\w+/versions/\\w+.json")) {
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
            } else if (target.matches("/services/\\w+/versions/\\w+/uses.json")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "GET":
                        String temp = target.substring(10, target.length() - 10);
                        int i = temp.indexOf("/");
                        int ii = temp.indexOf("/", i+1);
                        getVersionUses(response, temp.substring(0, i), temp.substring(ii + 1));
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
        version.status = versionData.status;
        cur.addVersion(version);
        dataAccess.save(cur);
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
        version.status = versionData.status;
        version.links = new LinkedList<>();
        for (Link link : versionData.links) {
            if (link.name != null && !link.name.isEmpty() && link.url != null && !link.url.isEmpty()) {
                if (!link.url.toLowerCase().startsWith("http://") && !link.url.toLowerCase().startsWith("https://")) {
                    link.url = "http://" + link.url;
                }
                version.links.add(link);
            }
        }
        Collections.sort(versionData.links, new Comparator<Link>(){
            @Override
            public int compare(Link o1, Link o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        version.operations = new LinkedList<>();
        for (Link link : versionData.operations) {
            if (link.name != null && !link.name.isEmpty() && link.url != null && !link.url.isEmpty()) {
                version.operations.add(link);
            }
        }
        Collections.sort(versionData.operations, new Comparator<Link>(){
            @Override
            public int compare(Link o1, Link o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        versionData.uses1.addAll(versionData.uses2);
        versionData.uses1.addAll(versionData.uses3);
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
                if (version.uses == null) {
                    version.uses = new LinkedList<>();
                }
                version.uses.add(serviceRef);
                addUsedBy(usesData.serviceId, usesData.versionId, cur.getId(), version.api, usesData.scope);
            }
        }
        dataAccess.save(cur);
        
        warningProcessor.scanServices();
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
        dataAccess.save(cur);
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
        dataAccess.save(cur);
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
        dataAccess.save(cur);
    }

    private void getVersionUses(HttpServletResponse response, String serviceId, String versionId) throws IOException {
        logger.info("serviceId {} versionId {}", serviceId, versionId);
        response.setContentType("application/json;charset=utf-8");
        Service service = dataAccess.getService(serviceId);
        Version version = service.findVersion(versionId);
        if (version == null) {
            return;
        }
        List<VersionView> versionHeaders = dataAccess.getVersionVeiw();
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            jw.beginArray();
            if (versionHeaders != null && !versionHeaders.isEmpty()) {
                for (VersionView versionHeader : versionHeaders) {
                    if (!versionHeader.serviceId.equals(serviceId)) {
                        if (version.uses != null && !version.uses.isEmpty()) {
                            for (ServiceRef ref : version.uses) {
                                if (ref.service.equals(versionHeader.serviceId) && ref.version.equals(versionHeader.versionId)) {
                                    versionHeader.scope = ref.scope;
                                }
                            }
                        }
                        //TODO: check status of version, don't include versions that are retiring or retired and there is no existing link
                        gson.toJson(versionHeader, VersionView.class, jw);
                    }
                }
            }
            jw.endArray();
        }
    }

}
