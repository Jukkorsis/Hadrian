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
import com.northernwall.hadrian.access.AccessException;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import com.northernwall.hadrian.service.dao.PutVipData;
import java.io.IOException;
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
public class VipHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(VipHandler.class);

    private final AccessHelper accessHelper;
    private final DataAccess dataAccess;
    private final WorkItemProcessor workItemProcess;

    public VipHandler(AccessHelper accessHelper, DataAccess dataAccess, WorkItemProcessor workItemProcess) {
        this.accessHelper = accessHelper;
        this.dataAccess = dataAccess;
        this.workItemProcess = workItemProcess;
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
                        } else {
                            throw new RuntimeException("Unknown vip operation");
                        }
                        break;
                    case "PUT":
                        if (target.matches("/v1/vip/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            String vipId = target.substring(8, target.length());
                            updateVip(request, vipId);
                        } else {
                            throw new RuntimeException("Unknown vip operation");
                        }
                        break;
                    case "DELETE":
                        if (target.matches("/v1/vip/\\w+-\\w+-\\w+-\\w+-\\w+/\\w+-\\w+-\\w+-\\w+-\\w+")) {
                            logger.info("Handling {} request {}", request.getMethod(), target);
                            String serviceId = target.substring(8, 44);
                            String vipId = target.substring(45);
                            deleteVip(request, serviceId, vipId);
                        } else {
                            throw new RuntimeException("Unknown vip operation");
                        }
                        break;
                    default:
                        throw new RuntimeException("Unknown vip operation");
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

    private void createVip(Request request) throws IOException {
        Vip vip = Util.fromJson(request, Vip.class);

        Service service = dataAccess.getService(vip.getServiceId());
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "add a vip");
        Team team = dataAccess.getTeam(service.getTeamId());

        //Check for duplicate VIP
        List<Vip> vips = dataAccess.getVips(vip.getServiceId());
        for (Vip temp : vips) {
            if (temp.getVipName().equals(vip.getVipName())) {
                return;
            }
            if (temp.getDns().equals(vip.getDns())
                    && temp.getDomain().equals(vip.getDomain())
                    && temp.getVipPort() == vip.getVipPort()) {
                return;
            }
        }

        vip.setStatus("Creating...");
        dataAccess.saveVip(vip);

        WorkItem workItem = new WorkItem(Const.TYPE_VIP, Const.OPERATION_CREATE, user, team, service, null, vip, null);
        dataAccess.saveWorkItem(workItem);
        workItemProcess.sendWorkItem(workItem);
    }

    private void updateVip(Request request, String vipId) throws IOException {
        PutVipData putVipData = Util.fromJson(request, PutVipData.class);

        Vip vip = dataAccess.getVip(putVipData.serviceId, vipId);
        if (vip == null) {
            throw new RuntimeException("Could not find vip");
        }
        Service service = dataAccess.getService(vip.getServiceId());
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "modify a vip");
        Team team = dataAccess.getTeam(service.getTeamId());

        vip.setStatus("Updating...");
        dataAccess.saveVip(vip);

        WorkItem workItem = new WorkItem(Const.TYPE_VIP, Const.OPERATION_UPDATE, user, team, service, null, vip, vip);
        workItem.getNewVip().external = putVipData.external;
        workItem.getNewVip().servicePort = putVipData.servicePort;
        dataAccess.saveWorkItem(workItem);
        workItemProcess.sendWorkItem(workItem);
    }

    private void deleteVip(Request request, String serviceId, String vipId) throws IOException {
        Service service = dataAccess.getService(serviceId);
        if (service == null) {
            throw new RuntimeException("Could not find service");
        }
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "delete a vip");
        Team team = dataAccess.getTeam(service.getTeamId());

        Vip vip = dataAccess.getVip(serviceId, vipId);
        if (vip == null) {
            logger.info("Could not find vip with id {}", vipId);
            return;
        }

        vip.setStatus("Deleting...");
        dataAccess.updateVip(vip);
        
        WorkItem workItem = new WorkItem(Const.TYPE_VIP, Const.OPERATION_DELETE, user, team, service, null, vip, null);
        dataAccess.saveWorkItem(workItem);
        workItemProcess.sendWorkItem(workItem);
    }

}