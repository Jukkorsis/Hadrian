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

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.access.Access;
import com.northernwall.hadrian.access.AccessException;
import com.northernwall.hadrian.webhook.WebHookSender;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.VipRef;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.service.dao.PostHostData;
import com.northernwall.hadrian.service.dao.PostHostVipData;
import com.northernwall.hadrian.service.dao.PutHostData;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
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
public class HostHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(HostHandler.class);

    private final Access access;
    private final Config config;
    private final DataAccess dataAccess;
    private final WebHookSender webHookSender;

    public HostHandler(Access access, Config config, DataAccess dataAccess, WebHookSender webHookSender) {
        this.access = access;
        this.config = config;
        this.dataAccess = dataAccess;
        this.webHookSender = webHookSender;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.startsWith("/v1/host/")) {
                switch (request.getMethod()) {
                    case "POST":
                        if (target.matches("/v1/host/host")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            createHosts(request);
                        } else if (target.matches("/v1/host/vips")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            addVIPs(request);
                        } else if (target.matches("/v1/host/backfill")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            backfillHosts(request);
                        } else {
                            throw new RuntimeException("Unknown host operation");
                        }
                        break;
                    case "PUT":
                        if (target.matches("/v1/host/host")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            updateHost(request);
                        } else if (target.matches("/v1/host/restart")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            restartHost(request);
                        } else {
                            throw new RuntimeException("Unknown host operation");
                        }
                        break;
                    case "DELETE":
                        if (target.matches("/v1/host/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            deleteHost(request, target.substring(9, target.length()));
                        } else if (target.matches("/v1/host/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            deleteVIP(request, target.substring(9, target.length() - 37), target.substring(46, target.length()));
                        } else {
                            throw new RuntimeException("Unknown host operation");
                        }
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
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

    private void createHosts(Request request) throws IOException {
        PostHostData postHostData = Util.fromJson(request, PostHostData.class);
        Service service = dataAccess.getService(postHostData.serviceId);
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        User user = access.checkIfUserCanModify(request, service.getTeamId(), "add a host");

        if (postHostData.count < 1) {
            throw new RuntimeException("count must to at least 1");
        } else if (postHostData.count > 10) {
            logger.warn("Reducing count to 10, was {}", postHostData.count);
            postHostData.count = 10;
        }

        if (!config.dataCenters.contains(postHostData.dataCenter)) {
            new RuntimeException("Unknown data center");
        }
        if (!config.networks.contains(postHostData.network)) {
            new RuntimeException("Unknown network");
        }
        if (!config.envs.contains(postHostData.env)) {
            new RuntimeException("Unknown env");
        }
        if (!config.sizes.contains(postHostData.size)) {
            new RuntimeException("Unknown size");
        }

        //calc host name
        String prefix = postHostData.dataCenter + "-" + postHostData.network + "-" + service.getServiceAbbr() + "-";
        int len = prefix.length();
        int num = 0;
        List<Host> hosts = dataAccess.getHosts(postHostData.serviceId);
        for (Host existingHost : hosts) {
            String existingHostName = existingHost.getHostName();
            if (existingHostName.startsWith(prefix) && existingHostName.length() > len) {
                String numPart = existingHostName.substring(len);
                try {
                    int temp = Integer.parseInt(numPart);
                    if (temp > num) {
                        num = temp;
                    }
                } catch (Exception e) {
                    logger.warn("Error parsing int from last part of {}", existingHostName);
                }
            }
        }
        num++;
        for (int c = 0; c < postHostData.count; c++) {
            String numStr = Integer.toString(num + c);
            numStr = "000".substring(numStr.length()) + numStr;

            Host host = new Host(prefix + numStr,
                    postHostData.serviceId,
                    "Creating",
                    postHostData.dataCenter,
                    postHostData.network,
                    postHostData.env,
                    postHostData.size);
            dataAccess.saveHost(host);
            webHookSender.createHost(service, host, user);
        }
    }

    private void updateHost(Request request) throws IOException {
        PutHostData putHostData = Util.fromJson(request, PutHostData.class);
        Service service = null;
        Host firstHost = null;
        WorkItem firstWorkItem = null;
        WorkItem workItem = null;
        User user = null;

        if (!config.envs.contains(putHostData.env)) {
            new RuntimeException("Unknown env");
        }
        if (!config.sizes.contains(putHostData.size)) {
            new RuntimeException("Unknown size");
        }

        for (Map.Entry<String, String> entry : putHostData.hosts.entrySet()) {
            if (entry.getValue().equalsIgnoreCase("true")) {
                Host host = dataAccess.getHost(entry.getKey());
                if (host != null && host.getServiceId().equals(putHostData.serviceId) && host.getStatus().equals(Const.NO_STATUS)) {
                    if (workItem == null) {
                        service = dataAccess.getService(host.getServiceId());
                        if (service == null) {
                            throw new RuntimeException("Could not find service");
                        }
                        user = access.checkIfUserCanModify(request, service.getTeamId(), "update a host");
                        access.checkIfUserCanModify(request, service.getTeamId(), "modify a host");
                        host.setStatus("Updating...");
                        dataAccess.saveHost(host);
                        firstHost = host;
                        firstWorkItem = WorkItem.createUpdateHost(
                                host.getHostId(),
                                putHostData.env,
                                putHostData.size,
                                putHostData.version,
                                user.getUsername());
                        workItem = firstWorkItem;
                    } else {
                        workItem.setNextId(host.getHostId());
                        dataAccess.saveWorkItem(workItem);
                        host.setStatus("Update Queued");
                        dataAccess.saveHost(host);
                        workItem = WorkItem.createUpdateHost(
                                host.getHostId(),
                                putHostData.env,
                                putHostData.size,
                                putHostData.version,
                                user.getUsername());
                    }
                }
            }
        }
        if (workItem != null) {
            dataAccess.saveWorkItem(workItem);
        }
        if (firstWorkItem != null) {
            webHookSender.updateHost(service, firstHost, firstWorkItem, user);
        }
    }

    private void restartHost(Request request) throws IOException {
    }

    private void deleteHost(Request request, String id) throws IOException {
        Host host = dataAccess.getHost(id);
        if (host == null) {
            logger.info("Could not find host with id {}", id);
            return;
        }
        Service service = dataAccess.getService(host.getServiceId());
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        User user = access.checkIfUserCanModify(request, service.getTeamId(), "deleting a host");
        host.setStatus("Deleting...");
        dataAccess.updateHost(host);
        webHookSender.deleteHost(service, host, user);
    }

    private void backfillHosts(Request request) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String s = reader.readLine();
        while (s != null && !s.isEmpty()) {
            String[] parts = s.split(",");
            if (parts.length == 6
                    && config.dataCenters.contains(parts[2])
                    && config.networks.contains(parts[3])
                    && config.envs.contains(parts[4])
                    && config.sizes.contains(parts[5])) {
                for (Service service : dataAccess.getServices()) {
                    if (service.getServiceAbbr().equalsIgnoreCase(parts[1])) {
                        Host host = new Host(parts[0],
                                service.getServiceId(),
                                Const.NO_STATUS,
                                parts[2],
                                parts[3],
                                parts[4],
                                parts[5]);
                        dataAccess.saveHost(host);
                    }
                }
            }
            s = reader.readLine();
        }
    }

    private void addVIPs(Request request) throws IOException {
        PostHostVipData data = Util.fromJson(request, PostHostVipData.class);
        Service service = dataAccess.getService(data.serviceId);
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        User user = access.checkIfUserCanModify(request, service.getTeamId(), "add a host vip");
        List<Host> hosts = dataAccess.getHosts(data.serviceId);
        List<Vip> vips = dataAccess.getVips(data.serviceId);
        for (Map.Entry<String, String> entry : data.hosts.entrySet()) {
            if (entry.getValue().equalsIgnoreCase("true")) {
                boolean found = false;
                for (Host host : hosts) {
                    if (entry.getKey().equals(host.getHostId())) {
                        found = true;
                        for (Map.Entry<String, String> entry2 : data.vips.entrySet()) {
                            if (entry2.getValue().equalsIgnoreCase("true")) {
                                boolean found2 = false;
                                for (Vip vip : vips) {
                                    if (entry2.getKey().equals(vip.getVipId())) {
                                        found2 = true;
                                        if (host.getNetwork().equals(vip.getNetwork())) {
                                            dataAccess.saveVipRef(new VipRef(host.getHostId(), vip.getVipId(), "Adding..."));
                                            webHookSender.addHostVip(service, host, vip, user);
                                        } else {
                                            logger.warn("Request to add {} to {} reject because they are not on the same network", host.getHostName(), vip.getVipName());
                                        }
                                    }
                                }
                                if (!found2) {
                                    logger.error("Asked to add host(s) to vip {}, but vip is not on service {}", entry2.getKey(), data.serviceId);
                                }
                            }
                        }
                    }
                }
                if (!found) {
                    logger.error("Asked to add vip(s) to host {}, but host is not on service {}", entry.getKey(), data.serviceId);
                }
            }
        }
    }

    private void deleteVIP(Request request, String hostId, String vipId) throws IOException {
        VipRef vipRef = dataAccess.getVipRef(hostId, vipId);
        Host host = dataAccess.getHost(hostId);
        if (host == null) {
            throw new RuntimeException("Could not find host");
        }
        Vip vip = dataAccess.getVip(vipId);
        if (vip == null) {
            throw new RuntimeException("Could not find vip");
        }
        Service service = dataAccess.getService(host.getServiceId());
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        User user = access.checkIfUserCanModify(request, service.getTeamId(), "delete host vip");
        vipRef.setStatus("Removing...");
        dataAccess.updateVipRef(vipRef);
        webHookSender.deleteHostVip(service, host, vip, user);
    }

}
