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

import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.webhook.WebHookSender;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.service.dao.PutVipData;
import java.io.IOException;
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
public class VipHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(VipHandler.class);

    private final DataAccess dataAccess;
    private final WebHookSender webHookHelper;

    public VipHandler(DataAccess dataAccess, WebHookSender urlHelper) {
        this.webHookHelper = urlHelper;
        this.dataAccess = dataAccess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.startsWith("/v1/vip/")) {
                switch (request.getMethod()) {
                    case "POST":
                        if (target.matches("/v1/vip/vip")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            createVip(request);
                            response.setStatus(200);
                            request.setHandled(true);
                        }
                        break;
                    case "PUT":
                        if (target.matches("/v1/vip/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            updateVip(request, target.substring(8, target.length()));
                            response.setStatus(200);
                            request.setHandled(true);
                        }
                        break;
                    case "DELETE":
                        if (target.matches("/v1/vip/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            deleteVip(target.substring(8, target.length()));
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

    private void createVip(Request request) throws IOException {
        Vip vip = Util.fromJson(request, Vip.class);
        
        //Check for duplicate VIP
        List<Vip> vips = dataAccess.getVips(vip.getServiceId());
        for (Vip temp : vips) {
            if (temp.getVipName().equals(vip.getVipName())) {
                return;
            }
            if (temp.getNetwork().equals(vip.getNetwork()) &&
                    temp.getDns().equals(vip.getDns()) &&
                    temp.getVipPort() == vip.getVipPort()) {
                return;
            }
        }

        vip.setStatus("Creating...");
        dataAccess.saveVip(vip);

        Service service = dataAccess.getService(vip.getServiceId());
        webHookHelper.postVip(service, vip);
    }

    private void updateVip(Request request, String vipId) throws IOException {
        PutVipData putVipData = Util.fromJson(request, PutVipData.class);

        Vip vip = dataAccess.getVip(vipId);
        vip.setStatus("Updating...");
        dataAccess.saveVip(vip);
        Service service = dataAccess.getService(vip.getServiceId());
        
        WorkItem workItem = WorkItem.createUpdateVip(
                vip.getVipId(), 
                putVipData.external, 
                putVipData.servicePort);
        dataAccess.saveWorkItem(workItem);
        webHookHelper.putVip(service, vip, workItem);
    }

    private void deleteVip(String id) throws IOException {
        Vip vip = dataAccess.getVip(id);
        if (vip == null) {
            logger.info("Could not find vip with id {}", id);
            return;
        }
        vip.setStatus("Deleting...");
        dataAccess.updateVip(vip);
        Service service = dataAccess.getService(vip.getServiceId());
        webHookHelper.deleteVip(service, vip);
    }

}
