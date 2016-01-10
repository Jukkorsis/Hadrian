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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.VipRef;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.webhook.dao.CallbackData;
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
    private final WebHookSender webHookSender;
    private final Timer timerProcess;
    private final Meter meterSuccess;
    private final Meter meterFail;

    public WebHookCallbackHandler(DataAccess dataAccess, WebHookSender webHookSender, MetricRegistry metricRegistry) {
        this.dataAccess = dataAccess;
        this.webHookSender = webHookSender;
        
        timerProcess = metricRegistry.timer("webhook.callback.process");
        meterSuccess = metricRegistry.meter("webhook.callback.success");
        meterFail = metricRegistry.meter("webhook.callback.fail");
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.startsWith("/webhook/callback") && request.getMethod().equals(Const.HTTP_POST)) {
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
        Context context = timerProcess.time();
        try {
            CallbackData callbackData = Util.fromJson(request, CallbackData.class);
            WorkItem workItem = dataAccess.getWorkItem(callbackData.requestId);
            if (workItem == null) {
                throw new RuntimeException("Could not find work item " + callbackData.requestId);
            }
            dataAccess.deleteWorkItem(callbackData.requestId);
            
            boolean status = callbackData.status.equalsIgnoreCase(Const.WEB_HOOK_STATUS_SUCCESS);
            if (status) {
                meterSuccess.mark();
            } else {
                meterFail.mark();
            }
            
            if (workItem.getType().equalsIgnoreCase(Const.TYPE_SERVICE)) {
                if (workItem.getOperation().equalsIgnoreCase(Const.OPERATION_CREATE)) {
                    createService(workItem, status);
                    return;
                }
            } else if (workItem.getType().equalsIgnoreCase(Const.TYPE_HOST)) {
                if (workItem.getOperation().equalsIgnoreCase(Const.OPERATION_CREATE)) {
                    createHost(workItem, status);
                    return;
                } else if (workItem.getOperation().equalsIgnoreCase(Const.OPERATION_UPDATE)) {
                    updateHost(workItem, status);
                    return;
                } else if (workItem.getOperation().equalsIgnoreCase(Const.OPERATION_DELETE)) {
                    deleteHost(workItem, status);
                    return;
                }
            } else if (workItem.getType().equalsIgnoreCase(Const.TYPE_VIP)) {
                if (workItem.getOperation().equalsIgnoreCase(Const.OPERATION_CREATE)) {
                    createVip(workItem, status);
                    return;
                } else if (workItem.getOperation().equalsIgnoreCase(Const.OPERATION_UPDATE)) {
                    updateVip(workItem, status);
                    return;
                } else if (workItem.getOperation().equalsIgnoreCase(Const.OPERATION_DELETE)) {
                    deleteVip(workItem, status);
                    return;
                }
            } else if (workItem.getType().equalsIgnoreCase(Const.TYPE_HOST_VIP)) {
                if (workItem.getOperation().equalsIgnoreCase("add")) {
                    addHostVip(workItem, status);
                    return;
                } else if (workItem.getOperation().equalsIgnoreCase(Const.OPERATION_DELETE)) {
                    deleteHostVip(workItem, status);
                    return;
                }
            }
            throw new RuntimeException("Unknown callback, " + workItem.getType() + " " + workItem.getOperation());
        } finally {
            context.stop();
        }
    }

    private void createService(WorkItem workItem, boolean status) throws IOException {
        Service service = dataAccess.getService(workItem.getService().serviceId);
        if (service == null) {
            logger.warn("Could not find service {} being created", workItem.getService().serviceId);
            return;
        }
        if (status) {
        } else {
            logger.warn("Callback for {} failed with status {}", service.getServiceId(), status);
        }
    }

    private void createHost(WorkItem workItem, boolean status) throws IOException {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            logger.warn("Could not find host {} being created", workItem.getHost().hostId);
            return;
        }
        if (status) {
            host.setStatus(Const.NO_STATUS);
            dataAccess.updateHost(host);
        } else {
            logger.warn("Callback for {} failed with status {}", host.getHostId(), status);
            dataAccess.deleteHost(host.getServiceId(), host.getHostId());
        }
    }

    private void updateHost(WorkItem workItem, boolean status) throws IOException {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            logger.warn("Could not find host {} being updated", workItem.getHost().hostId);
            return;
        }
        if (status) {
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

    private void deleteHost(WorkItem workItem, boolean status) throws IOException {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            logger.warn("Could not find host {} to delete.", workItem.getHost().hostId);
            return;
        }
        if (status) {
            dataAccess.deleteHost(host.getServiceId(), host.getHostId());
        } else {
            logger.warn("Callback for {} failed with status {}", host.getHostId(), status);
            host.setStatus(Const.NO_STATUS);
            dataAccess.updateHost(host);
        }
    }

    private void createVip(WorkItem workItem, boolean status) throws IOException {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            logger.warn("Could not find vip {} being created", workItem.getVip().vipId);
            return;
        }
        if (status) {
            vip.setStatus(Const.NO_STATUS);
            dataAccess.updateVip(vip);
        } else {
            logger.warn("Callback for {} failed with status {}", vip.getVipId(), status);
            dataAccess.deleteVip(vip.getServiceId(), vip.getVipId());
        }
    }

    private void updateVip(WorkItem workItem, boolean status) throws IOException {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            logger.warn("Could not find vip {} being updated", workItem.getVip().vipId);
            return;
        }
        if (status) {
            vip.setStatus(Const.NO_STATUS);
            vip.setExternal(workItem.getNewVip().external);
            vip.setServicePort(workItem.getNewVip().servicePort);
            dataAccess.updateVip(vip);
        } else {
            logger.warn("Callback for {} failed with status {}", workItem.getVip().vipId, status);
        }
    }

    private void deleteVip(WorkItem workItem, boolean status) throws IOException {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            logger.error("Could not find end point {} to delete.", workItem.getVip().vipId);
            return;
        }
        if (status) {
            dataAccess.deleteVipRefs(vip.getVipId());
            dataAccess.deleteVip(vip.getServiceId(), vip.getVipId());
        } else {
            logger.warn("Callback for {} failed with status {}", vip.getVipId(), status);
            vip.setStatus(Const.NO_STATUS);
            dataAccess.updateVip(vip);
        }
    }

    private void addHostVip(WorkItem workItem, boolean status) {
        VipRef vipRef = dataAccess.getVipRef(workItem.getHost().hostId, workItem.getVip().vipId);
        if (vipRef == null) {
            logger.error("Could not find end point ref {} {} to create.", workItem.getHost().hostId, workItem.getVip().vipId);
            return;
        }
        if (status) {
            vipRef.setStatus(Const.NO_STATUS);
            dataAccess.updateVipRef(vipRef);
        } else {
            logger.warn("Callback for {} {} failed with status {}", vipRef.getHostId(), vipRef.getVipId(), status);
            dataAccess.deleteVipRef(vipRef.getHostId(), vipRef.getVipId());
        }
    }

    private void deleteHostVip(WorkItem workItem, boolean status) {
        VipRef vipRef = dataAccess.getVipRef(workItem.getHost().hostId, workItem.getVip().vipId);
        if (vipRef == null) {
            logger.error("Could not find end point ref {} {} to delete.", workItem.getHost().hostId, workItem.getVip().vipId);
            return;
        }
        if (status) {
            dataAccess.deleteVipRef(vipRef.getHostId(), vipRef.getVipId());
        } else {
            logger.warn("Callback for {} {} failed with status {}", vipRef.getHostId(), vipRef.getVipId(), status);
            vipRef.setStatus(Const.NO_STATUS);
            dataAccess.updateVipRef(vipRef);
        }
    }

}
