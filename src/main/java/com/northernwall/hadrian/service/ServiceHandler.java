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
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.domain.CustomFunction;
import com.northernwall.hadrian.domain.DataStore;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.VipRef;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ServiceRef;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.service.dao.GetAuditData;
import com.northernwall.hadrian.service.dao.GetCustomFunctionData;
import com.northernwall.hadrian.service.dao.GetDataStoreData;
import com.northernwall.hadrian.service.dao.GetHostData;
import com.northernwall.hadrian.service.dao.GetModuleData;
import com.northernwall.hadrian.service.dao.GetNotUsesData;
import com.northernwall.hadrian.service.dao.GetServiceData;
import com.northernwall.hadrian.service.dao.GetServiceRefData;
import com.northernwall.hadrian.service.dao.GetServicesData;
import com.northernwall.hadrian.service.dao.GetVipData;
import com.northernwall.hadrian.service.dao.GetVipRefData;
import com.northernwall.hadrian.service.dao.PostServiceData;
import com.northernwall.hadrian.service.dao.PostServiceRefData;
import com.northernwall.hadrian.service.dao.PutServiceData;
import com.northernwall.hadrian.service.helper.ReadAvailabilityRunnable;
import com.northernwall.hadrian.service.helper.ReadMavenVersionsRunnable;
import com.northernwall.hadrian.service.helper.ReadVersionRunnable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    private final Config config;
    private final MavenHelper mavenHelper;
    private final InfoHelper infoHelper;
    private final Gson gson;
    private final ExecutorService executorService;
    private final DateFormat format;

    public ServiceHandler(AccessHelper accessHelper, DataAccess dataAccess, WorkItemProcessor workItemProcess, Config config, MavenHelper mavenHelper, InfoHelper infoHelper) {
        this.accessHelper = accessHelper;
        this.dataAccess = dataAccess;
        this.workItemProcess = workItemProcess;
        this.config = config;
        this.mavenHelper = mavenHelper;
        this.infoHelper = infoHelper;
        gson = new Gson();

        executorService = Executors.newFixedThreadPool(20);

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

        List<Future> futures = new LinkedList<>();

        List<Module> modules = dataAccess.getModules(id);
        Collections.sort(modules);
        for (Module module : modules) {
            GetModuleData getModuleData = GetModuleData.create(module, config);
            futures.add(executorService.submit(new ReadMavenVersionsRunnable(getModuleData, mavenHelper)));
            getServiceData.modules.add(getModuleData);
        }

        List<Vip> vips = dataAccess.getVips(id);
        Collections.sort(vips);
        for (Vip vip : vips) {
            GetModuleData getModuleData = null;
            for (GetModuleData temp : getServiceData.modules) {
                if (vip.getModuleId().equals(temp.moduleId)) {
                    getModuleData = temp;
                }
            }
            if (getModuleData != null) {
                GetVipData getVipData = GetVipData.create(vip);
                getModuleData.addVip(getVipData);
            }
        }

        List<Host> hosts = dataAccess.getHosts(id);
        Collections.sort(hosts);
        for (Host host : hosts) {
            GetModuleData getModuleData = null;
            for (GetModuleData temp : getServiceData.modules) {
                if (host.getModuleId().equals(temp.moduleId)) {
                    getModuleData = temp;
                }
            }
            if (getModuleData != null) {
                GetHostData getHostData = GetHostData.create(host);
                futures.add(executorService.submit(new ReadVersionRunnable(getHostData, getModuleData, infoHelper)));
                futures.add(executorService.submit(new ReadAvailabilityRunnable(getHostData, getModuleData, infoHelper)));
                for (VipRef vipRef : dataAccess.getVipRefsByHost(getHostData.hostId)) {
                    GetVipRefData getVipRefData = GetVipRefData.create(vipRef);
                    for (GetVipData vip : getModuleData.getVips(host.getNetwork())) {
                        if (vip.vipId.equals(getVipRefData.vipId)) {
                            getVipRefData.vipName = vip.vipName;
                        }
                    }
                    getHostData.vipRefs.add(getVipRefData);
                }
                getModuleData.addHost(getHostData);
            }
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
            now.add(Calendar.DATE, -15);
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
        Collections.sort(auditData.audits);

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

        if (postServiceData.serviceType.equals(Const.SERVICE_TYPE_SHARED_LIBRARY)) {
            postServiceData.gitMode = Const.GIT_MODE_FLAT;
        }

        Service service = new Service(
                postServiceData.serviceAbbr.toUpperCase(),
                postServiceData.serviceName,
                postServiceData.teamId,
                postServiceData.description,
                postServiceData.serviceType,
                postServiceData.gitMode,
                postServiceData.gitProject);

        dataAccess.saveService(service);
        
        Map<String, String> notes = new HashMap<>();
        notes.put("name", service.getServiceName());
        notes.put("abbr", service.getServiceAbbr());
        createAudit(service.getServiceId(), user.getUsername(), Const.TYPE_SERVICE, Const.OPERATION_CREATE, notes);
    }

    private void updateService(Request request, String id) throws IOException {
        PutServiceData putServiceData = Util.fromJson(request, PutServiceData.class);
        Service service = dataAccess.getService(id);
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        accessHelper.checkIfUserCanModify(request, service.getTeamId(), "modify a service");

        service.setServiceAbbr(putServiceData.serviceAbbr.toUpperCase());
        service.setServiceName(putServiceData.serviceName);
        service.setDescription(putServiceData.description);

        dataAccess.updateService(service);
    }

    private void createServiceRef(Request request, String clientId) throws IOException {
        PostServiceRefData postServiceRefData = Util.fromJson(request, PostServiceRefData.class);
        Service clientService = dataAccess.getService(clientId);
        if (clientService == null) {
            throw new RuntimeException("Could not find service");
        }
        User user = accessHelper.checkIfUserCanModify(request, clientService.getTeamId(), "add a service ref");
        for (Entry<String, String> entry : postServiceRefData.uses.entrySet()) {
            if (entry.getValue().equalsIgnoreCase("true")) {
                String serverId = entry.getKey();
                Service serverService = dataAccess.getService(serverId);
                if (serverService != null) {
                    ServiceRef ref = new ServiceRef(clientId, serverId);
                    dataAccess.saveServiceRef(ref);
                    Map<String, String> notes = new HashMap<>();
                    notes.put("uses", serverService.getServiceAbbr());
                    createAudit(clientId, user.getUsername(), Const.TYPE_SERVICE_REF, Const.OPERATION_CREATE, notes);
                    notes = new HashMap<>();
                    notes.put("use_by", clientService.getServiceAbbr());
                    createAudit(serverId, user.getUsername(), Const.TYPE_SERVICE_REF, Const.OPERATION_CREATE, notes);
                }
            }
        }
    }

    private void deleteServiceRef(Request request, String clientId, String serverId) {
        Service clientService = dataAccess.getService(clientId);
        if (clientService == null) {
            return;
        }
        Service serverService = dataAccess.getService(serverId);
        if (serverService == null) {
            return;
        }
        User user = accessHelper.checkIfUserCanModify(request, clientService.getTeamId(), "delete a service ref");
        dataAccess.deleteServiceRef(clientId, serverId);
        Map<String, String> notes = new HashMap<>();
        notes.put("uses", serverService.getServiceAbbr());
        createAudit(clientId, user.getUsername(), Const.TYPE_SERVICE_REF, Const.OPERATION_DELETE, notes);
        notes = new HashMap<>();
        notes.put("use_by", clientService.getServiceAbbr());
        createAudit(serverId, user.getUsername(), Const.TYPE_SERVICE_REF, Const.OPERATION_DELETE, notes);
    }

    private void createAudit(String serviceId, String requestor, String type, String operation, Map<String, String> notes) {
        Audit audit = new Audit();
        audit.serviceId = serviceId;
        audit.timePerformed = new Date();
        audit.timeRequested = new Date();
        audit.requestor = requestor;
        audit.type = type;
        audit.operation = operation;
        audit.notes = gson.toJson(notes);
        dataAccess.saveAudit(audit, " ");
    }

}
