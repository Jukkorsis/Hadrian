package com.northernwall.hadrian.workItem;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.VipRef;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.dao.CallbackData;
import java.io.IOException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkItemProcessor {

    private final static Logger logger = LoggerFactory.getLogger(WorkItemProcessor.class);

    private final DataAccess dataAccess;
    private final WorkItemSender webHookSender;
    private final Timer timerProcess;
    private final Timer timerCalback;
    private final Meter meterSuccess;
    private final Meter meterFail;

    public WorkItemProcessor(DataAccess dataAccess, WorkItemSender webHookSender, MetricRegistry metricRegistry) {
        this.dataAccess = dataAccess;
        this.webHookSender = webHookSender;

        timerProcess = metricRegistry.timer("workItem.sendWorkItem");
        timerCalback = metricRegistry.timer("workItem.callback.process");
        meterSuccess = metricRegistry.meter("workItem.callback.success");
        meterFail = metricRegistry.meter("workItem.callback.fail");
    }

    public void sendWorkItem(WorkItem workItem) throws IOException {
        boolean done = false;
        Timer.Context context = timerProcess.time();
        try {
            done = webHookSender.sendWorkItem(workItem);
        } finally {
            context.stop();
        }
        if (done) {
            logger.info("Work item sender says work item  {} has been process, no callback expected.", workItem.getId());
            CallbackData callbackData = new CallbackData();
            callbackData.requestId = workItem.getId();
            callbackData.errorCode = 0;
            callbackData.errorDescription = " ";
            callbackData.status = Const.WORK_ITEM_STATUS_SUCCESS;
            callbackData.output = "no output";
            processCallback(callbackData);
        }
    }

    public void processCallback(CallbackData callbackData) throws IOException {
        Timer.Context context = timerCalback.time();
        try {
            WorkItem workItem = dataAccess.getWorkItem(callbackData.requestId);
            if (workItem == null) {
                throw new RuntimeException("Could not find work item " + callbackData.requestId);
            }
            dataAccess.deleteWorkItem(callbackData.requestId);

            boolean status = callbackData.status.equalsIgnoreCase(Const.WORK_ITEM_STATUS_SUCCESS);
            if (status) {
                meterSuccess.mark();
            } else {
                meterFail.mark();
            }

            String notes = " ";
            if (workItem.getType().equalsIgnoreCase(Const.TYPE_SERVICE)) {
                if (workItem.getOperation().equalsIgnoreCase(Const.OPERATION_CREATE)) {
                    createService(workItem, status);
                    notes = "template=" + workItem.getService().template;
                } else {
                    throw new RuntimeException("Unknown callback " + workItem.getType() + " " + workItem.getOperation());
                }
            } else if (workItem.getType().equalsIgnoreCase(Const.TYPE_HOST)) {
                if (workItem.getOperation().equalsIgnoreCase(Const.OPERATION_CREATE)) {
                    createHost(workItem, status);
                    notes = "env=" + workItem.getHost().env + " size=" + workItem.getHost().size;
                } else if (workItem.getOperation().equalsIgnoreCase(Const.OPERATION_DEPLOY)) {
                    deploySoftware(workItem, status);
                    notes = "version=" + workItem.getHost().version;
                } else if (workItem.getOperation().equalsIgnoreCase(Const.OPERATION_DELETE)) {
                    deleteHost(workItem, status);
                } else {
                    throw new RuntimeException("Unknown callback " + workItem.getType() + " " + workItem.getOperation());
                }
            } else if (workItem.getType().equalsIgnoreCase(Const.TYPE_VIP)) {
                if (workItem.getOperation().equalsIgnoreCase(Const.OPERATION_CREATE)) {
                    createVip(workItem, status);
                } else if (workItem.getOperation().equalsIgnoreCase(Const.OPERATION_UPDATE)) {
                    updateVip(workItem, status);
                } else if (workItem.getOperation().equalsIgnoreCase(Const.OPERATION_DELETE)) {
                    deleteVip(workItem, status);
                } else {
                    throw new RuntimeException("Unknown callback " + workItem.getType() + " " + workItem.getOperation());
                }
            } else if (workItem.getType().equalsIgnoreCase(Const.TYPE_HOST_VIP)) {
                if (workItem.getOperation().equalsIgnoreCase("add")) {
                    addHostVip(workItem, status);
                } else if (workItem.getOperation().equalsIgnoreCase(Const.OPERATION_DELETE)) {
                    deleteHostVip(workItem, status);
                } else {
                    throw new RuntimeException("Unknown callback " + workItem.getType() + " " + workItem.getOperation());
                }
            } else {
                throw new RuntimeException("Unknown callback " + workItem.getType() + " " + workItem.getOperation());
            }
            if (status) {
                Audit audit = new Audit();
                audit.serviceId = workItem.getService().serviceId;
                audit.timePerformed = new Date();
                audit.timeRequested = workItem.getRequestDate();
                audit.requestor = workItem.getUsername();
                audit.type = workItem.getType();
                audit.operation = workItem.getOperation();
                if (workItem.getHost() != null) {
                    audit.hostname = workItem.getHost().hostName;
                }
                if (workItem.getVip() != null) {
                    audit.vipname = workItem.getVip().vipName;
                }
                audit.notes = notes;
                dataAccess.saveAudit(audit, callbackData.output);
            }
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
            if (workItem.getNextId() != null) {
                WorkItem nextWorkItem = dataAccess.getWorkItem(workItem.getNextId());
                if (nextWorkItem != null) {
                    host.setStatus("Deploying...");
                    dataAccess.updateHost(host);
                    webHookSender.sendWorkItem(nextWorkItem);
                } else {
                    logger.warn("Odd, the deploy work item {} for create host {} could not be found", workItem.getNextId(), host.getHostName());
                    host.setStatus(Const.NO_STATUS);
                    dataAccess.updateHost(host);
                }
            } else {
                logger.warn("Odd, create host {} work item has no deploy work item id", host.getHostName());
            }
        } else {
            logger.warn("Callback for {} failed with status {}", host.getHostId(), status);
            dataAccess.deleteHost(host.getServiceId(), host.getHostId());
            if (workItem.getNextId() != null) {
                dataAccess.deleteWorkItem(workItem.getNextId());
            } else {
                logger.warn("Odd, the failed create host {} work item has no deploy work item id", host.getHostName());
            }
        }
    }

    private void deploySoftware(WorkItem workItem, boolean status) throws IOException {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            logger.warn("Could not find host {} being updated", workItem.getHost().hostId);
            return;
        }
        if (status) {
            host.setStatus(Const.NO_STATUS);
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
            nextHost.setStatus("Deploying...");
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
