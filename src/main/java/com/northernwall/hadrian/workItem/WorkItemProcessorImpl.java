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
package com.northernwall.hadrian.workItem;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.gson.Gson;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.GMT;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.action.CreateHostAction;
import com.northernwall.hadrian.workItem.action.DeploySoftwareAction;
import com.northernwall.hadrian.workItem.action.RestartHostAction;
import com.northernwall.hadrian.workItem.dao.CallbackData;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkItemProcessorImpl implements WorkItemProcessor {

    private final static Logger logger = LoggerFactory.getLogger(WorkItemProcessor.class);

    private final DataAccess dataAccess;
    private final WorkItemSender workItemSender;
    private final Timer timerProcess;
    private final Timer timerCalback;
    private final Meter meterSuccess;
    private final Meter meterFail;
    private final Gson gson;
    private final CreateHostAction createHostAction;
    private final DeploySoftwareAction deploySoftwareAction;
    private final RestartHostAction restartHostAction;

    public WorkItemProcessorImpl(DataAccess dataAccess, WorkItemSender workItemSender, MetricRegistry metricRegistry) {
        this.dataAccess = dataAccess;
        this.workItemSender = workItemSender;

        timerProcess = metricRegistry.timer("workItem.sendWorkItem");
        timerCalback = metricRegistry.timer("workItem.callback.process");
        meterSuccess = metricRegistry.meter("workItem.callback.success");
        meterFail = metricRegistry.meter("workItem.callback.fail");
        gson = new Gson();

        createHostAction = new CreateHostAction(dataAccess, this);
        deploySoftwareAction = new DeploySoftwareAction(dataAccess, this);
        restartHostAction = new RestartHostAction(dataAccess, this);
    }

    @Override
    public void sendWorkItem(WorkItem workItem) throws IOException {
        Result result;
        Timer.Context context = timerProcess.time();
        try {
            result = workItemSender.sendWorkItem(workItem);
        } finally {
            context.stop();
        }

        switch (result) {
            case success:
                logger.info("Work item sender says work item  {} has been process, no callback expected.", workItem.getId());
                break;
            case error:
                logger.warn("Work item sender says work item  {} failed to be process, no callback expected.", workItem.getId());
                break;
            case wip:
                logger.info("Work item sender says work item  {} is being processed.", workItem.getId());
                return;
        }

        CallbackData callbackData = new CallbackData();
        callbackData.requestId = workItem.getId();
        callbackData.errorCode = 0;
        callbackData.errorDescription = " ";
        callbackData.status = result;
        callbackData.output = null;
        processCallback(callbackData);
    }

    @Override
    public void processCallback(CallbackData callbackData) throws IOException {
        Timer.Context context = timerCalback.time();
        try {
            WorkItem workItem = dataAccess.getWorkItem(callbackData.requestId);
            if (workItem == null) {
                throw new RuntimeException("Could not find work item " + callbackData.requestId);
            }
            if (callbackData.status == null) {
                throw new RuntimeException("Callback is missing status, " + callbackData.requestId);
            }

            switch (callbackData.status) {
                case success:
                    meterSuccess.mark();
                    break;
                case error:
                    meterFail.mark();
                    break;
                case wip:
                    logger.warn("ProcessCallback should never be called for WIP");
                    return;
            }

            dataAccess.deleteWorkItem(callbackData.requestId);

            Map<String, String> notes = new HashMap<>();
            switch (workItem.getType()) {
                case module:
                    switch (workItem.getOperation()) {
                        case create:
                            notes.put("template", workItem.getMainModule().template);
                            notes.put("type", workItem.getMainModule().moduleType.toString());
                            break;
                        case update:
                            break;
                        case delete:
                            break;
                        default:
                            throw new RuntimeException("Unknown callback " + workItem.getType() + " " + workItem.getOperation());
                    }
                    break;
                case host:
                    switch (workItem.getOperation()) {
                        case create:
                            createHostAction.process(workItem, callbackData.status);
                            notes.put("env", workItem.getHost().env);
                            notes.put("size", workItem.getHost().size);
                            notes.put("reason", workItem.getHost().reason);
                            break;
                        case deploy:
                            deploySoftwareAction.process(workItem, callbackData.status);
                            if (workItem.getHost().version != null) {
                                notes.put("version", workItem.getHost().version);
                            }
                            if (workItem.getHost().versionUrl != null) {
                                notes.put("versionUrl", workItem.getHost().versionUrl);
                            }
                            if (workItem.getHost().configVersion != null) {
                                notes.put("config_Version", workItem.getHost().configVersion);
                            }
                            notes.put("reason", workItem.getHost().reason);
                            break;
                        case restart:
                            restartHostAction.process(workItem, callbackData.status);
                            notes.put("reason", workItem.getHost().reason);
                            break;
                        case delete:
                            deleteHost(workItem, callbackData.status);
                            notes.put("reason", workItem.getHost().reason);
                            break;
                        default:
                            throw new RuntimeException("Unknown callback " + workItem.getType() + " " + workItem.getOperation());
                    }
                    break;
                case vip:
                    switch (workItem.getOperation()) {
                        case create:
                            notes.put("protocol", workItem.getVip().protocol);
                            notes.put("dns", workItem.getVip().dns + "." + workItem.getVip().domain);
                            notes.put("vip_port", Integer.toString(workItem.getVip().vipPort));
                            notes.put("service_port", Integer.toString(workItem.getVip().servicePort));
                            notes.put("external", Boolean.toString(workItem.getVip().external));
                            createVip(workItem, callbackData.status);
                            break;
                        case update:
                            notes.put("protocol", workItem.getVip().protocol);
                            notes.put("dns", workItem.getVip().dns + "." + workItem.getVip().domain);
                            notes.put("vip_port", Integer.toString(workItem.getVip().vipPort));
                            notes.put("service_port", Integer.toString(workItem.getVip().servicePort));
                            notes.put("external", Boolean.toString(workItem.getVip().external));
                            updateVip(workItem, callbackData.status);
                            break;
                        case delete:
                            deleteVip(workItem, callbackData.status);
                            break;
                        default:
                            throw new RuntimeException("Unknown callback " + workItem.getType() + " " + workItem.getOperation());
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown callback " + workItem.getType() + " " + workItem.getOperation());
            }
            Audit audit = new Audit();
            audit.serviceId = workItem.getService().serviceId;
            audit.timePerformed = GMT.getGmtAsDate();
            audit.timeRequested = workItem.getRequestDate();
            audit.requestor = workItem.getUsername();
            audit.type = workItem.getType();
            audit.operation = workItem.getOperation();
            audit.successfull = (callbackData.status == Result.success);
            if (workItem.getMainModule() != null) {
                audit.moduleName = workItem.getMainModule().moduleName;
            }
            if (workItem.getHost() != null) {
                audit.hostName = workItem.getHost().hostName;
            }
            if (workItem.getVip() != null) {
                audit.vipName = workItem.getVip().dns;
            }
            if (notes.isEmpty()) {
                audit.notes = "";
            } else {
                audit.notes = gson.toJson(notes);
            }
            dataAccess.saveAudit(audit, callbackData.output);
            if (callbackData.status == Result.error) {
                deleteNextWorkItem(workItem.getNextId());
            }
        } finally {
            context.stop();
        }
    }

    private void deleteNextWorkItem(String nextId) {
        if (nextId != null) {
            WorkItem nextWorkItem = dataAccess.getWorkItem(nextId);
            deleteNextWorkItem(nextWorkItem.getNextId());
            WorkItem workItem = dataAccess.getWorkItem(nextId);
            if (workItem != null) {
                if (workItem.getType() == Type.host) {
                    Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
                    if (host != null) {
                        host.setStatus(Const.NO_STATUS);
                        dataAccess.updateHost(host);
                    }
                }
                dataAccess.deleteWorkItem(nextId);
            }
        }
    }

    private void deleteHost(WorkItem workItem, Result result) throws IOException {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            logger.warn("Could not find host {} to delete.", workItem.getHost().hostId);
            return;
        }
        if (result == Result.success) {
            dataAccess.deleteHost(host.getServiceId(), host.getHostId());
        } else {
            logger.warn("Callback for {} failed with status {}", host.getHostId(), result);
            host.setStatus(Const.NO_STATUS);
            dataAccess.updateHost(host);
        }
    }

    private void createVip(WorkItem workItem, Result result) throws IOException {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            logger.warn("Could not find vip {} being created", workItem.getVip().vipId);
            return;
        }
        if (result == Result.success) {
            vip.setStatus(Const.NO_STATUS);
            dataAccess.updateVip(vip);
        } else {
            logger.warn("Callback for {} failed with status {}", vip.getVipId(), result);
            dataAccess.deleteVip(vip.getServiceId(), vip.getVipId());
        }
    }

    private void updateVip(WorkItem workItem, Result result) throws IOException {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            logger.warn("Could not find vip {} being updated", workItem.getVip().vipId);
            return;
        }
        if (result == Result.success) {
            vip.setStatus(Const.NO_STATUS);
            vip.setExternal(workItem.getVip().external);
            vip.setServicePort(workItem.getVip().servicePort);
            dataAccess.updateVip(vip);
        } else {
            logger.warn("Callback for {} failed with status {}", workItem.getVip().vipId, result);
        }
    }

    private void deleteVip(WorkItem workItem, Result result) throws IOException {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            logger.error("Could not find end point {} to delete.", workItem.getVip().vipId);
            return;
        }
        if (result == Result.success) {
            dataAccess.deleteVip(vip.getServiceId(), vip.getVipId());
        } else {
            logger.warn("Callback for {} failed with status {}", vip.getVipId(), result);
            vip.setStatus(Const.NO_STATUS);
            dataAccess.updateVip(vip);
        }
    }

}
