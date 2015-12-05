/*
 * Copyright 2015 Richard Thurston.
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
package com.northernwall.hadrian.webhook;

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.VipRef;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.WorkItem;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class WebHookCallbackHandler extends AbstractHandler {
    private final static Logger logger = LoggerFactory.getLogger(WebHookCallbackHandler.class);
    

    private final DataAccess dataAccess;
    private final WebHookSender webHookSender;

    public WebHookCallbackHandler(DataAccess dataAccess, WebHookSender webHookSender) {
        this.dataAccess = dataAccess;
        this.webHookSender = webHookSender;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.startsWith("/webhook/callback") && request.getMethod().equals("POST")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                processCallback(target.substring(18), request);
                response.setStatus(200);
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void processCallback(String id, Request request) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        int status = Integer.parseInt(reader.readLine());
        WorkItem workItem = dataAccess.getWorkItem(id);
        if (workItem == null) {
            throw new RuntimeException("Could not find work item " + id);
        }
        dataAccess.deleteWorkItem(id);
        if (workItem.getType().equalsIgnoreCase("service")) {
            if (workItem.getOperation().equalsIgnoreCase("create")) {
                createService(workItem, status);
                return;
            }
        } else if (workItem.getType().equalsIgnoreCase("host")) {
            if (workItem.getOperation().equalsIgnoreCase("create")) {
                createHost(workItem, status);
                return;
            } else if (workItem.getOperation().equalsIgnoreCase("update")) {
                updateHost(workItem, status);
                return;
            } else if (workItem.getOperation().equalsIgnoreCase("delete")) {
                deleteHost(workItem, status);
                return;
            }
        } else if (workItem.getType().equalsIgnoreCase("vip")) {
            if (workItem.getOperation().equalsIgnoreCase("create")) {
                createVip(workItem, status);
                return;
            } else if (workItem.getOperation().equalsIgnoreCase("update")) {
                updateVip(workItem, status);
                return;
            } else if (workItem.getOperation().equalsIgnoreCase("delete")) {
                deleteVip(workItem, status);
                return;
            }
        } else if (workItem.getType().equalsIgnoreCase("hostvip")) {
            if (workItem.getOperation().equalsIgnoreCase("add")) {
                addHostVip(workItem, status);
                return;
            } else if (workItem.getOperation().equalsIgnoreCase("delete")) {
                deleteHostVip(workItem, status);
                return;
            }
        }
        throw new RuntimeException("Unknown callback, " +workItem.getType() + " " + workItem.getOperation());
    }

    private void createService(WorkItem workItem, int status) throws IOException {
        Service service = dataAccess.getService(workItem.getService().serviceId);
        if (service == null) {
            logger.warn("Could not find service {} being created", workItem.getService().serviceId);
            return;
        }
        if (status >= 300) {
            logger.warn("Callback for {} failed with status {}", service.getServiceId(), status);
        }
    }

    private void createHost(WorkItem workItem, int status) throws IOException {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            logger.warn("Could not find host {} being created", workItem.getHost().hostId);
            return;
        }
        if (status < 300) {
            host.setStatus(Const.NO_STATUS);
            dataAccess.updateHost(host);
        } else {
            logger.warn("Callback for {} failed with status {}", host.getHostId(), status);
            dataAccess.deleteHost(host.getServiceId(), host.getHostId());
        }
    }

    private void updateHost(WorkItem workItem, int status) throws IOException {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            logger.warn("Could not find host {} being updated", workItem.getHost().hostId);
            return;
        }
        if (status < 300) {
            host.setStatus(Const.NO_STATUS);
            host.setEnv(workItem.getNewHost().env);
            host.setSize(workItem.getNewHost().size);
            dataAccess.updateHost(host);
            
            if (workItem.getNextId() == null) {
                //No more hosts to update in the chain
                return;
            }
            
            WorkItem nextWorkItem = dataAccess.getWorkItem(workItem.getNextId());
            Host nextHost = dataAccess.getHost(nextWorkItem.getService().serviceId, nextWorkItem.getHost().hostId);
            if (nextHost == null) {
                logger.error("Finished updating {}, next work item is {}, but could not find it.", workItem.getHost().hostId, nextWorkItem.getHost().hostId);
                return;
            }
            nextHost.setStatus("Updating...");
            dataAccess.saveHost(nextHost);

            webHookSender.sendWorkItem(nextWorkItem);
        } else {
            logger.warn("Callback for {} failed with status {}", workItem.getHost().hostId, status);
            //TODO: need to find the remaining workitems and cancel them
        }
    }

    private void deleteHost(WorkItem workItem, int status) throws IOException {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            logger.warn("Could not find host {} to delete.", workItem.getHost().hostId);
            return;
        }
        if (status < 300) {
            dataAccess.deleteHost(host.getServiceId(), host.getHostId());
        } else {
            logger.warn("Callback for {} failed with status {}", host.getHostId(), status);
            host.setStatus(Const.NO_STATUS);
            dataAccess.updateHost(host);
        }
    }

    private void createVip(WorkItem workItem, int status) throws IOException {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            logger.warn("Could not find vip {} being created", workItem.getVip().vipId);
            return;
        }
        if (status < 300) {
            vip.setStatus(Const.NO_STATUS);
            dataAccess.updateVip(vip);
        } else {
            logger.warn("Callback for {} failed with status {}", vip.getVipId(), status);
            dataAccess.deleteVip(vip.getServiceId(), vip.getVipId());
        }
    }

    private void updateVip(WorkItem workItem, int status) throws IOException {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            logger.warn("Could not find vip {} being updated", workItem.getVip().vipId);
            return;
        }
        if (status < 300) {
            vip.setStatus(Const.NO_STATUS);
            vip.setExternal(workItem.getNewVip().external);
            vip.setServicePort(workItem.getNewVip().servicePort);
            dataAccess.updateVip(vip);
        } else {
            logger.warn("Callback for {} failed with status {}", workItem.getVip().vipId, status);
        }
    }

    private void deleteVip(WorkItem workItem, int status) throws IOException {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            logger.error("Could not find end point {} to delete.", workItem.getVip().vipId);
            return;
        }
        if (status < 300) {
            dataAccess.deleteVipRefs(vip.getVipId());
            dataAccess.deleteVip(vip.getServiceId(), vip.getVipId());
        } else {
            logger.warn("Callback for {} failed with status {}", vip.getVipId(), status);
            vip.setStatus(Const.NO_STATUS);
            dataAccess.updateVip(vip);
        }
    }

    private void addHostVip(WorkItem workItem, int status) {
        VipRef vipRef = dataAccess.getVipRef(workItem.getHost().hostId, workItem.getVip().vipId);
        if (vipRef == null) {
            logger.error("Could not find end point ref {} {} to create.", workItem.getHost().hostId, workItem.getVip().vipId);
            return;
        }
        if (status < 300) {
            vipRef.setStatus(Const.NO_STATUS);
            dataAccess.updateVipRef(vipRef);
        } else {
            logger.warn("Callback for {} {} failed with status {}", vipRef.getHostId(), vipRef.getVipId(), status);
            dataAccess.deleteVipRef(vipRef.getHostId(), vipRef.getVipId());
        }
    }

    private void deleteHostVip(WorkItem workItem, int status) {
        VipRef vipRef = dataAccess.getVipRef(workItem.getHost().hostId, workItem.getVip().vipId);
        if (vipRef == null) {
            logger.error("Could not find end point ref {} {} to delete.", workItem.getHost().hostId, workItem.getVip().vipId);
            return;
        }
        if (status < 300) {
            dataAccess.deleteVipRef(vipRef.getHostId(), vipRef.getVipId());
        } else {
            logger.warn("Callback for {} {} failed with status {}", vipRef.getHostId(), vipRef.getVipId(), status);
            vipRef.setStatus(Const.NO_STATUS);
            dataAccess.updateVipRef(vipRef);
        }
    }

}
