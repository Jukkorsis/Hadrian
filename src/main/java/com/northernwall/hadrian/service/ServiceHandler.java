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

import com.northernwall.hadrian.service.helper.InfoHelper;
import com.northernwall.hadrian.maven.MavenHelper;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.access.AccessException;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.CustomFunction;
import com.northernwall.hadrian.domain.DataStore;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.VipRef;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.ServiceRef;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.service.dao.GetAuditData;
import com.northernwall.hadrian.service.dao.GetCustomFunctionData;
import com.northernwall.hadrian.service.dao.GetDataStoreData;
import com.northernwall.hadrian.service.dao.GetHostData;
import com.northernwall.hadrian.service.dao.GetNotUsesData;
import com.northernwall.hadrian.service.dao.GetServiceData;
import com.northernwall.hadrian.service.dao.GetServiceRefData;
import com.northernwall.hadrian.service.dao.GetServicesData;
import com.northernwall.hadrian.service.dao.GetVipData;
import com.northernwall.hadrian.service.dao.GetVipRefData;
import com.northernwall.hadrian.service.dao.PostServiceData;
import com.northernwall.hadrian.service.dao.PostServiceRefData;
import com.northernwall.hadrian.service.dao.PutServiceData;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;
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

    private final AccessHelper accessHelper;
    private final DataAccess dataAccess;
    private final WorkItemProcessor workItemProcess;
    private final MavenHelper mavenhelper;
    private final InfoHelper infoHelper;
    private final Gson gson;
    private final ExecutorService es;
    private final DateFormat format;


    public ServiceHandler(AccessHelper accessHelper, DataAccess dataAccess, WorkItemProcessor workItemProcess, MavenHelper mavenhelper, InfoHelper infoHelper) {
        this.accessHelper = accessHelper;
        this.dataAccess = dataAccess;
        this.workItemProcess = workItemProcess;
        this.mavenhelper = mavenhelper;
        this.infoHelper = infoHelper;
        gson = new Gson();

        es = Executors.newFixedThreadPool(20);
        
        format = new SimpleDateFormat("MM/dd/yyyy");
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.startsWith("/v1/service")) {
                switch (request.getMethod()) {
                    case Const.HTTP_GET:
                        if (target.matches("/v1/service")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            getServices(response);
                            response.setStatus(200);
                            request.setHandled(true);
                        } else if (target.matches("/v1/service/\\w+-\\w+-\\w+-\\w+-\\w+/notuses")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            getServiceNotUses(response, target.substring(12, target.length() - 8));
                            response.setStatus(200);
                            request.setHandled(true);
                        } else if (target.matches("/v1/service/\\w+-\\w+-\\w+-\\w+-\\w+/audit")) {
                            String start = request.getParameter("start");
                            String end = request.getParameter("end");
                            logger.info("Handling {} request {} start {} end {}", request.getMethod(), target, start, end);
                            getServiceAudit(response, target.substring(12, target.length() - 6), start, end);
                            response.setStatus(200);
                            request.setHandled(true);
                        } else if (target.matches("/v1/service/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            getService(request, response, target.substring(12, target.length()));
                            response.setStatus(200);
                            request.setHandled(true);
                        }
                        break;
                    case Const.HTTP_POST:
                        if (target.matches("/v1/service/service")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            createService(request);
                            response.setStatus(200);
                            request.setHandled(true);
                        } else if (target.matches("/v1/service/\\w+-\\w+-\\w+-\\w+-\\w+/ref")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            createServiceRef(request, target.substring(12, target.length() - 4));
                            response.setStatus(200);
                            request.setHandled(true);
                        }
                        break;
                    case Const.HTTP_PUT:
                        if (target.matches("/v1/service/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            updateService(request, target.substring(12, target.length()));
                            response.setStatus(200);
                            request.setHandled(true);
                        }
                        break;
                    case Const.HTTP_DELETE:
                        if (target.matches("/v1/service/\\w+-\\w+-\\w+-\\w+-\\w+/uses/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            deleteServiceRef(request, target.substring(12, target.length() - 42), target.substring(54, target.length()));
                            response.setStatus(200);
                            request.setHandled(true);
                        }
                        break;
                }
            }
        } catch (AccessException e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target);
            response.setStatus(401);
            request.setHandled(true);
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
            request.setHandled(true);
        }
    }

    private void getServices(HttpServletResponse response) throws IOException {
        response.setContentType(Const.JSON);

        List<Service> services = dataAccess.getServices();
        GetServicesData getServicesData = new GetServicesData();
        for (Service service : services) {
            getServicesData.services.add(GetServiceData.create(service));
        }

        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(getServicesData, GetServicesData.class, jw);
        }
    }

    private void getService(Request request, HttpServletResponse response, String id) throws IOException {
        response.setContentType(Const.JSON);
        Service service = dataAccess.getService(id);
        if (service == null) {
            throw new RuntimeException("Could not find service with id '" + id + "'");
        }

        GetServiceData getServiceData = GetServiceData.create(service);
        getServiceData.canModify = accessHelper.canUserModify(request, service.getTeamId());

        List<Vip> vips = dataAccess.getVips(id);
        Collections.sort(vips);
        for (Vip vip : vips) {
            GetVipData getVipData = GetVipData.create(vip);
            getServiceData.vips.add(getVipData);
        }

        List<Future> futures = new LinkedList<>();
        List<Host> hosts = dataAccess.getHosts(id);
        Collections.sort(hosts);
        for (Host host : hosts) {
            GetHostData getHostData = GetHostData.create(host);
            futures.add(es.submit(new ReadVersionRunnable(getHostData, getServiceData)));
            futures.add(es.submit(new ReadAvailabilityRunnable(getHostData, getServiceData)));
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

        List<DataStore> dataStores = dataAccess.getDataStores(id);
        Collections.sort(dataStores);
        for (DataStore dataStore : dataStores) {
            GetDataStoreData getDataStoreData = GetDataStoreData.create(dataStore);
            getServiceData.dataStores.add(getDataStoreData);
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

        List<CustomFunction> customFunctions = dataAccess.getCustomFunctions(id);
        Collections.sort(customFunctions);
        for (CustomFunction customFunction : customFunctions) {
            GetCustomFunctionData getCustomFunctionData = GetCustomFunctionData.create(customFunction);
            getServiceData.customFunctions.add(getCustomFunctionData);
        }

        //TODO: make this a future also
        getServiceData.versions.addAll(mavenhelper.readMavenVersions(getServiceData.mavenGroupId, getServiceData.mavenArtifactId));

        waitForFutures(futures);

        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(getServiceData, GetServiceData.class, jw);
        }
    }

    private void waitForFutures(List<Future> futures) {
        for (int i = 0; i < 20; i++) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
            }
            futures.removeIf(new Predicate<Future>() {
                @Override
                public boolean test(Future t) {
                    return t.isDone();
                }
            });
            if (futures.isEmpty()) {
                return;
            }
        }
    }

    class ReadVersionRunnable implements Runnable {

        private final GetHostData getHostData;
        private final GetServiceData getServiceData;

        public ReadVersionRunnable(GetHostData getHostData, GetServiceData getServiceData) {
            this.getHostData = getHostData;
            this.getServiceData = getServiceData;
        }

        @Override
        public void run() {
            try {
                getHostData.version = infoHelper.readVersion(getHostData.hostName, getServiceData.versionUrl);
            } catch (IOException ex) {
            }
        }
    }

    class ReadAvailabilityRunnable implements Runnable {

        private final GetHostData getHostData;
        private final GetServiceData getServiceData;

        public ReadAvailabilityRunnable(GetHostData getHostData, GetServiceData getServiceData) {
            this.getHostData = getHostData;
            this.getServiceData = getServiceData;
        }

        @Override
        public void run() {
            try {
                getHostData.availability = infoHelper.readAvailability(getHostData.hostName, getServiceData.availabilityUrl);
            } catch (IOException ex) {
            }
        }
    }

    private void getServiceNotUses(HttpServletResponse response, String id) throws IOException {
        logger.info("got here {}", id);
        List<Service> services = dataAccess.getServices();
        List<ServiceRef> refs = dataAccess.getServiceRefsByClient(id);

        GetNotUsesData notUses = new GetNotUsesData();
        for (Service service : services) {
            if (!service.getServiceId().equals(id)) {
                boolean found = false;
                for (ServiceRef ref : refs) {
                    if (service.getServiceId().equals(ref.getServerServiceId())) {
                        found = true;
                    }
                }
                if (!found) {
                    GetServiceRefData ref = new GetServiceRefData();
                    ref.clientServiceId = id;
                    ref.serverServiceId = service.getServiceId();
                    ref.serviceName = service.getServiceName();
                    notUses.refs.add(ref);
                }
            }
        }

        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(notUses, GetNotUsesData.class, jw);
        }
    }

    private void getServiceAudit(HttpServletResponse response, String id, String start, String end) throws IOException {
        GetAuditData auditData = new GetAuditData();

        Date startDate = null;
        try {
            startDate = format.parse(start);
        } catch (ParseException ex) {
            Calendar now = Calendar.getInstance();
            now.add(Calendar.YEAR, -2);
            now.add(Calendar.DATE, -1);
            startDate = now.getTime();
        }
        Date endDate = null;
        try {
            endDate = format.parse(end);
        } catch (ParseException ex) {
            Calendar now = Calendar.getInstance();
            now.add(Calendar.DATE, 1);
            endDate = now.getTime();
        }
        auditData.audits = dataAccess.getAudit(id, startDate, endDate);

        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(auditData, GetAuditData.class, jw);
        }
    }

    private void createService(Request request) throws IOException {
        PostServiceData postServiceData = Util.fromJson(request, PostServiceData.class);
        User user = accessHelper.checkIfUserCanModify(request, postServiceData.teamId, "create a service");
        postServiceData.serviceAbbr = postServiceData.serviceAbbr.toLowerCase();

        for (Service temp : dataAccess.getServices(postServiceData.teamId)) {
            if (temp.getServiceAbbr().equals(postServiceData.serviceAbbr)) {
                logger.warn("A service already exists with that abbreviation, {}", postServiceData.serviceAbbr);
                return;
            }
        }

        Team team = dataAccess.getTeam(postServiceData.teamId);

        Service service = new Service(
                postServiceData.serviceAbbr,
                postServiceData.serviceName,
                postServiceData.teamId,
                postServiceData.description,
                postServiceData.runAs,
                team.getTeamAbbr() + "/" + postServiceData.gitPath,
                postServiceData.mavenGroupId,
                postServiceData.mavenArtifactId,
                postServiceData.artifactType,
                postServiceData.artifactSuffix,
                postServiceData.versionUrl,
                postServiceData.availabilityUrl,
                postServiceData.startCmdLine,
                postServiceData.stopCmdLine);

        dataAccess.saveService(service);
        WorkItem workItem = new WorkItem(Const.TYPE_SERVICE, Const.OPERATION_CREATE, user, team, service, null, null, null);
        workItem.getService().template = postServiceData.template;
        dataAccess.saveWorkItem(workItem);
        workItemProcess.sendWorkItem(workItem);
    }

    private void updateService(Request request, String id) throws IOException {
        PutServiceData putServiceData = Util.fromJson(request, PutServiceData.class);
        Service service = dataAccess.getService(id);
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        accessHelper.checkIfUserCanModify(request, service.getTeamId(), "modify a service");

        service.setServiceAbbr(putServiceData.serviceAbbr);
        service.setServiceName(putServiceData.serviceName);
        service.setDescription(putServiceData.description);
        service.setRunAs(putServiceData.runAs);
        service.setMavenGroupId(putServiceData.mavenGroupId);
        service.setMavenArtifactId(putServiceData.mavenArtifactId);
        service.setArtifactType(putServiceData.artifactType);
        service.setArtifactSuffix(putServiceData.artifactSuffix);
        service.setVersionUrl(putServiceData.versionUrl);
        service.setAvailabilityUrl(putServiceData.availabilityUrl);
        service.setStartCmdLine(putServiceData.startCmdLine);
        service.setStopCmdLine(putServiceData.stopCmdLine);

        dataAccess.updateService(service);
    }

    private void createServiceRef(Request request, String id) throws IOException {
        PostServiceRefData postServiceRefData = Util.fromJson(request, PostServiceRefData.class);
        Service service = dataAccess.getService(id);
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        accessHelper.checkIfUserCanModify(request, service.getTeamId(), "add a service ref");
        for (Entry<String, String> entry : postServiceRefData.uses.entrySet()) {
            if (entry.getValue().equalsIgnoreCase("true")) {
                ServiceRef ref = new ServiceRef(id, entry.getKey());
                dataAccess.saveServiceRef(ref);
            }
        }
    }

    private void deleteServiceRef(Request request, String clientId, String serviceId) {
        Service service = dataAccess.getService(clientId);
        if (service == null) {
            return;
        }
        accessHelper.checkIfUserCanModify(request, service.getTeamId(), "delete a service ref");
        dataAccess.deleteServiceRef(clientId, serviceId);
    }

}
