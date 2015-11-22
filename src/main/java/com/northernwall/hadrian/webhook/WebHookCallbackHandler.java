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
import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.VipRef;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.webhook.dao.CallbackResponse;
import java.io.IOException;
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
    private final WebHookSender urlHelper;

    public WebHookCallbackHandler(DataAccess dataAccess, WebHookSender urlHelper) {
        this.dataAccess = dataAccess;
        this.urlHelper = urlHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.startsWith("/webhook/callback") && request.getMethod().equals("POST")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                processCallback(request);
                response.setStatus(200);
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void processCallback(Request request) throws IOException {
        CallbackResponse data = Util.fromJson(request, CallbackResponse.class);
        if (data.type.equalsIgnoreCase("service")) {
            if (data.operation.equalsIgnoreCase("create")) {
                return;
            }
        } else if (data.type.equalsIgnoreCase("host")) {
            if (data.operation.equalsIgnoreCase("create")) {
                createHost(data);
                return;
            } else if (data.operation.equalsIgnoreCase("update")) {
                updateHost(data);
                return;
            } else if (data.operation.equalsIgnoreCase("delete")) {
                deleteHost(data);
                return;
            }
        } else if (data.type.equalsIgnoreCase("vip")) {
            if (data.operation.equalsIgnoreCase("create")) {
                createVip(data);
                return;
            } else if (data.operation.equalsIgnoreCase("update")) {
                updateVip(data);
                return;
            } else if (data.operation.equalsIgnoreCase("delete")) {
                deleteVip(data);
                return;
            }
        } else if (data.type.equalsIgnoreCase("hostvip")) {
            if (data.operation.equalsIgnoreCase("add")) {
                addHostVip(data);
                return;
            } else if (data.operation.equalsIgnoreCase("delete")) {
                deleteHostVip(data);
                return;
            }
        }
        throw new RuntimeException("Unknown callback, " +data.type + " " + data.operation);
    }

    private void createHost(CallbackResponse data) throws IOException {
        Host host = dataAccess.getHost(data.serviceId, data.hostId);
        if (data.status < 300) {
            host.setStatus(Const.NO_STATUS);
            dataAccess.updateHost(host);
        } else {
            logger.info("Callback for {} failed with status {}", data.hostId, data.status);
            dataAccess.deleteHost(data.serviceId, data.hostId);
        }
    }

    private void updateHost(CallbackResponse data) throws IOException {
        Host host = dataAccess.getHost(data.serviceId, data.hostId);
        if (data.status < 300) {
            host.setStatus(Const.NO_STATUS);
            dataAccess.updateHost(host);
            
            WorkItem workItem = dataAccess.getWorkItem(data.hostId);
            if (workItem == null) {
                logger.error("Could not find work item on callback, this should not happen!");
                return;
            }
            host.setEnv(workItem.getEnv());
            host.setSize(workItem.getSize());
            dataAccess.updateHost(host);
            
            //find the version of instance in the DB and update it's status
            if (workItem.getNextId() == null) {
                return;
            }
            Host nextHost = dataAccess.getHost(data.serviceId, workItem.getNextId());
            if (nextHost == null) {
                logger.error("Finished updating {}, next work item is {}, but could not find it.", data.hostId, workItem.getNextId());
                return;
            }
            nextHost.setStatus("Updating...");
            dataAccess.saveHost(nextHost);

            WorkItem nextWorkItem = dataAccess.getWorkItem(workItem.getNextId());
            Service service = dataAccess.getService(nextHost.getServiceId());
            User user = dataAccess.getUser(nextWorkItem.getUsername());
            urlHelper.updateHost(service, nextHost, nextWorkItem, user);
        } else {
            logger.info("Callback for {} failed with status {}", data.hostId, data.status);
            //todo, need to find the remaining workitems and cancel them
        }
    }

    private void deleteHost(CallbackResponse data) throws IOException {
        Host host = dataAccess.getHost(data.serviceId, data.hostId);
        if (host == null) {
            logger.error("Could not find host {} to delete.", data.hostId);
        } else if (data.status < 300) {
            dataAccess.deleteHost(data.serviceId, data.hostId);
        } else {
            logger.info("Callback for {} failed with status {}", data.hostId, data.status);
            host.setStatus(Const.NO_STATUS);
            dataAccess.updateHost(host);
        }
    }

    private void createVip(CallbackResponse data) throws IOException {
        Vip vip = dataAccess.getVip(data.serviceId, data.vipId);
        if (data.status < 300) {
            vip.setStatus(Const.NO_STATUS);
            dataAccess.updateVip(vip);
        } else {
            logger.info("Callback for {} failed with status {}", data.vipId, data.status);
            dataAccess.deleteVip(data.serviceId, data.vipId);
        }
    }

    private void updateVip(CallbackResponse data) throws IOException {
        Vip vip = dataAccess.getVip(data.serviceId, data.vipId);
        if (data.status < 300) {
            vip.setStatus(Const.NO_STATUS);
            
            WorkItem workItem = dataAccess.getWorkItem(data.vipId);
            if (workItem == null) {
                logger.error("Could not find work item on callback, this should not happen!");
            } else {
                vip.setExternal(workItem.getExternal());
                vip.setServicePort(workItem.getServicePort());
            }
            
            dataAccess.updateVip(vip);
        } else {
            logger.info("Callback for {} failed with status {}", data.vipId, data.status);
            //todo, need to find the remaining workitems and cancel them
        }
    }

    private void deleteVip(CallbackResponse data) throws IOException {
        Vip vip = dataAccess.getVip(data.serviceId, data.vipId);
        if (vip == null) {
            logger.error("Could not find end point {} to delete.", data.vipId);
        } else if (data.status < 300) {
            dataAccess.deleteVipRefs(data.vipId);
            dataAccess.deleteVip(data.serviceId, data.vipId);
        } else {
            logger.info("Callback for {} failed with status {}", data.vipId, data.status);
            vip.setStatus(Const.NO_STATUS);
            dataAccess.updateVip(vip);
        }
    }

    private void addHostVip(CallbackResponse data) {
        VipRef vipRef = dataAccess.getVipRef(data.hostId, data.vipId);
        if (data.status < 300) {
            vipRef.setStatus(Const.NO_STATUS);
            dataAccess.updateVipRef(vipRef);
        } else {
            logger.info("Callback for {} failed with status {}", data.vipId, data.status);
            dataAccess.deleteVip(data.serviceId, data.vipId);
        }
    }

    private void deleteHostVip(CallbackResponse data) {
        VipRef vipRef = dataAccess.getVipRef(data.hostId, data.vipId);
        if (vipRef == null) {
            logger.error("Could not find end point ref {} {} to delete.", data.hostId, data.vipId);
        } else if (data.status < 300) {
            dataAccess.deleteVipRef(data.hostId, data.vipId);
        } else {
            logger.info("Callback for {} {} failed with status {}", data.hostId, data.vipId, data.status);
            vipRef.setStatus(Const.NO_STATUS);
            dataAccess.updateVipRef(vipRef);
        }
    }

}
