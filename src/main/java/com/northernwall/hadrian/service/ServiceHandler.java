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
package com.northernwall.hadrian.service;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.VipRef;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.ServiceRef;
import com.northernwall.hadrian.service.dao.GetHostData;
import com.northernwall.hadrian.service.dao.GetServiceData;
import com.northernwall.hadrian.service.dao.GetServiceRefData;
import com.northernwall.hadrian.service.dao.GetVipData;
import com.northernwall.hadrian.service.dao.GetVipRefData;
import com.northernwall.hadrian.service.dao.PostServiceData;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
public class ServiceHandler extends AbstractHandler {
    private final static Logger logger = LoggerFactory.getLogger(ServiceHandler.class);

    private final DataAccess dataAccess;
    private final MavenHelper mavenhelper;
    private final InfoHelper infoHelper;
    private final Gson gson;

    public ServiceHandler(DataAccess dataAccess, MavenHelper mavenhelper, InfoHelper infoHelper) {
        this.dataAccess = dataAccess;
        this.mavenhelper = mavenhelper;
        this.infoHelper = infoHelper;
        gson = new Gson();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.startsWith("/v1/service/")) {
                switch (request.getMethod()) {
                    case "GET":
                        if (target.matches("/v1/service/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            getService(response, target.substring(12, target.length()));
                            response.setStatus(200);
                            request.setHandled(true);
                        }
                        break;
                    case "POST":
                        if (target.matches("/v1/service/service")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            createService(request);
                            response.setStatus(200);
                            request.setHandled(true);
                        }
                        break;
                }
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void getService(HttpServletResponse response, String id) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        Service service = dataAccess.getService(id);
        if (service == null) {
            throw new RuntimeException("Could not find service with id '" + id + "'");
        }

        GetServiceData getServiceData = GetServiceData.create(service);
        
        for (Vip vip : dataAccess.getVips(id)) {
            GetVipData getVipData = GetVipData.create(vip);
            getServiceData.vips.add(getVipData);

        }
        
        for (Host host : dataAccess.getHosts(id)) {
            GetHostData getHostData = GetHostData.create(host);
            getHostData.version = infoHelper.readVersion(getHostData.hostName, getServiceData.versionUrl);
            getHostData.availability = infoHelper.readAvailability(getHostData.hostName, getServiceData.availabilityUrl);
            for (VipRef vipRef : dataAccess.getVipRefsByHost(getHostData.hostId)) {
                GetVipRefData getVipRefData = GetVipRefData.create(vipRef);
                for (GetVipData vip : getServiceData.vips) {
                    if (vip.vipId.equals(getVipRefData.vipId)) {
                        getVipRefData.vipName = vip.vipName;
                    }
                }
                getHostData.vipRefs.add(getVipRefData);
            }
            getServiceData.hosts.add(getHostData);
        }
        
        for (ServiceRef ref : dataAccess.getServiceRefsByClient(id)) {
            GetServiceRefData tempRef = GetServiceRefData.create(ref);
            tempRef.serviceName = dataAccess.getService(ref.getServerServiceId()).getServiceName();
            getServiceData.uses.add(tempRef);
        }
        
        for (ServiceRef ref : dataAccess.getServiceRefsByServer(id)) {
            GetServiceRefData tempRef = GetServiceRefData.create(ref);
            tempRef.serviceName = dataAccess.getService(ref.getClientServiceId()).getServiceName();
            getServiceData.usedBy.add(tempRef);
        }
        
        getServiceData.versions.addAll(mavenhelper.readMavenVersions(getServiceData.mavenGroupId, getServiceData.mavenArtifactId));

        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(getServiceData, GetServiceData.class, jw);
        }
    }

    private void createService(Request request) throws IOException {
        PostServiceData postServiceData = Util.fromJson(request, PostServiceData.class);
        postServiceData.serviceAbbr = postServiceData.serviceAbbr.toLowerCase();
        
        for (Service temp : dataAccess.getServices(postServiceData.teamId)) {
            if (temp.getServiceAbbr().equals(postServiceData.serviceAbbr)) {
                logger.warn("A service already exists with that abbreviation, {}", postServiceData.serviceAbbr);
                return;
            }
        }
        
        Service service = new Service(
                postServiceData.serviceAbbr,
                postServiceData.serviceName,
                postServiceData.teamId,
                postServiceData.description,
                postServiceData.mavenGroupId,
                postServiceData.mavenArtifactId,
                postServiceData.versionUrl,
                postServiceData.availabilityUrl);
        
        dataAccess.saveService(service);
    }

}
