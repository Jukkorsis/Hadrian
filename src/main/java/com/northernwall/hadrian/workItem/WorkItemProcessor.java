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

import com.northernwall.hadrian.workItem.action.HostDeleteAction;
import com.northernwall.hadrian.workItem.action.HostDeployAction;
import com.northernwall.hadrian.workItem.action.HostRestartAction;
import com.northernwall.hadrian.workItem.action.HostCreateAction;
import com.northernwall.hadrian.workItem.action.ModuleDeleteAction;
import com.northernwall.hadrian.workItem.action.ModuleUpdateAction;
import com.northernwall.hadrian.workItem.action.Action;
import com.northernwall.hadrian.workItem.action.ModuleCreateAction;
import com.google.gson.Gson;
import com.northernwall.hadrian.ConfigHelper;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.workItem.action.HostSmokeTestAction;
import com.northernwall.hadrian.workItem.action.HostVipAddAction;
import com.northernwall.hadrian.workItem.action.HostVipDisableAction;
import com.northernwall.hadrian.workItem.action.HostVipEnableAction;
import com.northernwall.hadrian.workItem.action.HostVipRemoveAction;
import com.northernwall.hadrian.workItem.action.ServiceCreateAction;
import com.northernwall.hadrian.workItem.action.ServiceDeleteAction;
import com.northernwall.hadrian.workItem.action.ServiceUpdateAction;
import com.northernwall.hadrian.workItem.action.VipCreateAction;
import com.northernwall.hadrian.workItem.action.VipDeleteAction;
import com.northernwall.hadrian.workItem.action.VipFixAction;
import com.northernwall.hadrian.workItem.action.VipUpdateAction;
import com.northernwall.hadrian.workItem.dao.CallbackData;
import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.dsh.metrics.MetricRegistry;
import org.dsh.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkItemProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(WorkItemProcessor.class);

    private final Parameters parameters;
    private final ConfigHelper configHelper;
    private final DataAccess dataAccess;
    private final OkHttpClient client;
    private final Gson gson;
    private final MetricRegistry metricRegistry;
    private final Action serviceCreate;
    private final Action serviceUpdate;
    private final Action serviceDelete;
    private final Action moduleCreate;
    private final Action moduleUpdate;
    private final Action moduleDelete;
    private final Action hostCreate;
    private final Action hostDeploy;
    private final Action hostRestart;
    private final Action hostSmokeTest;
    private final Action hostDelete;
    private final Action vipCreate;
    private final Action vipUpdate;
    private final Action vipDelete;
    private final Action vipFix;
    private final Action hostVipEnable;
    private final Action hostVipDisable;
    private final Action hostVipAdd;
    private final Action hostVipRemove;
    private final ExecutorService executor;

    public WorkItemProcessor(Parameters parameters, ConfigHelper configHelper, DataAccess dataAccess, OkHttpClient client, Gson gson, MetricRegistry metricRegistry) {
        this.parameters = parameters;
        this.configHelper = configHelper;
        this.dataAccess = dataAccess;
        this.client = client;
        this.gson = gson;
        this.metricRegistry = metricRegistry;

        serviceCreate = constructAction("serviceCreate", ServiceCreateAction.class);
        serviceUpdate = constructAction("serviceUpdate", ServiceUpdateAction.class);
        serviceDelete = constructAction("serviceDelete", ServiceDeleteAction.class);

        moduleCreate = constructAction("moduleCreate", ModuleCreateAction.class);
        moduleUpdate = constructAction("moduleUpdate", ModuleUpdateAction.class);
        moduleDelete = constructAction("moduleDelete", ModuleDeleteAction.class);

        hostCreate = constructAction("hostCreate", HostCreateAction.class);
        hostDeploy = constructAction("hostDeploy", HostDeployAction.class);
        hostRestart = constructAction("hostRestart", HostRestartAction.class);
        hostSmokeTest = constructAction("hostSmokeTest", HostSmokeTestAction.class);
        hostDelete = constructAction("hostDelete", HostDeleteAction.class);

        vipCreate = constructAction("vipCreate", VipCreateAction.class);
        vipUpdate = constructAction("vipUpdate", VipUpdateAction.class);
        vipDelete = constructAction("vipDelete", VipDeleteAction.class);
        vipFix = constructAction("vipFix", VipFixAction.class);

        hostVipEnable = constructAction("hostVipEnable", HostVipEnableAction.class);
        hostVipDisable = constructAction("hostVipDisable", HostVipDisableAction.class);
        hostVipAdd = constructAction("hostVipAdd", HostVipAddAction.class);
        hostVipRemove = constructAction("hostVipRemove", HostVipRemoveAction.class);

        executor = Executors.newFixedThreadPool(10);
    }

    private Action constructAction(String name, Class defaultClass) {
        String factoryName = parameters.getString("action." + name, null);
        try {
            Class c;
            if (factoryName != null && !factoryName.isEmpty()) {
                c = Class.forName(factoryName);
            } else {
                c = defaultClass;
                factoryName = defaultClass.getName();
            }
            Action action = (Action) c.newInstance();
            action.init(name, dataAccess, parameters, configHelper, client, gson);
            LOGGER.info("Constructed action {} with {}", name, factoryName);
            return action;
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Could not build Action " + name + ", could not find class " + factoryName, ex);
        } catch (InstantiationException ex) {
            throw new RuntimeException("Could not build Action " + name + ", could not instantiation class " + factoryName, ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Could not build Action " + name + ", could not access class " + factoryName, ex);
        }
    }

    public void processWorkItem(final WorkItem workItem) throws IOException {
        if (workItem == null) {
            return;
        }
        workItem.setNextId(null);
        dataAccess.saveWorkItem(workItem);

        executor.submit(() -> {
            process(workItem);
        });
    }

    public void processWorkItems(final List<WorkItem> workItems) throws IOException {
        if (workItems == null || workItems.isEmpty()) {
            return;
        }

        if (workItems.size() == 1) {
            processWorkItem(workItems.get(0));
            return;
        }

        int size = workItems.size();
        for (int i = 0; i < size; i++) {
            WorkItem workItem = workItems.get(i);
            if (i == (size - 1)) {
                workItem.setNextId(null);
            } else {
                workItem.setNextId(workItems.get(i + 1).getId());
            }
            dataAccess.saveWorkItem(workItem);
        }

        executor.submit(() -> {
            process(workItems.get(0));
        });
    }

    private void process(WorkItem workItem) {
        Action action = getAction(workItem);
        Result result = Result.error;

        Timer timer = metricRegistry.timer(
                "action-process",
                "action", action.getName());
        try {
            action.updateStatus(workItem);
            result = action.process(workItem);
        } catch (Exception e) {
            LOGGER.warn("Failure while performing action {}, {}", action.getName(), e.getMessage());
        } finally {
            timer.stop("result", result.toString());
        }

        switch (result) {
            case success:
                action.success(workItem);
                action.recordAudit(workItem, new HashMap<>(), result, null);
                LOGGER.info("Work item {} has been successfully processed, no callback expected. {}", action.getName(), workItem.getId());
                dataAccess.deleteWorkItem(workItem.getId());
                dataAccess.saveWorkItemStatus(workItem.getId(), 200);
                startNext(workItem);
                break;
            case error:
                action.error(workItem);
                action.recordAudit(workItem, new HashMap<>(), result, null);
                LOGGER.warn("Work item {} failed to be process, no callback expected. {}", action.getName(), workItem.getId());
                dataAccess.deleteWorkItem(workItem.getId());
                dataAccess.saveWorkItemStatus(workItem.getId(), 502);
                stopNext(workItem);
                break;
            case wip:
                LOGGER.info("Work item {} is being processed, waiting for callback. {}", action.getName(), workItem.getId());
        }
    }

    public void processCallback(CallbackData callbackData) {
        WorkItem workItem = dataAccess.getWorkItem(callbackData.requestId);
        Action action = getAction(workItem);
        Result result = Result.error;

        Timer timer = metricRegistry.timer(
                "action-processCallback",
                "action", action.getName());
        try {
            result = action.processCallback(workItem, callbackData);
        } catch (Exception e) {
            LOGGER.warn("Failure while performing action calback {}, {}", action.getName(), e.getMessage());
        } finally {
            timer.stop("result", result.toString());
        }

        switch (result) {
            case success:
                action.success(workItem);
                action.recordAudit(workItem, createNotesFromCallback(callbackData), result, callbackData.output);
                LOGGER.info("Work item {} has been successfully processed. {}", action.getName(), workItem.getId());
                dataAccess.deleteWorkItem(workItem.getId());
                dataAccess.saveWorkItemStatus(workItem.getId(), 200);
                startNext(workItem);
                break;
            case error:
                action.error(workItem);
                action.recordAudit(workItem, createNotesFromCallback(callbackData), result, callbackData.output);
                LOGGER.warn("Work item {} failed to be process. {}", action.getName(), workItem.getId());
                dataAccess.deleteWorkItem(workItem.getId());
                dataAccess.saveWorkItemStatus(workItem.getId(), 502);
                stopNext(workItem);
                break;
            case wip:
                LOGGER.info("Work item {} is still being processed. {}", action.getName(), workItem.getId());
        }
    }

    private Map<String, String> createNotesFromCallback(CallbackData callbackData) {
        Map<String, String> notes = new HashMap<>();
        if (callbackData != null) {
            if (callbackData.errorCode != 0) {
                notes.put("error_code", Integer.toString(callbackData.errorCode));
            }
            if (callbackData.errorDescription != null && !callbackData.errorDescription.isEmpty()) {
                notes.put("error_desc", callbackData.errorDescription);
            }
        }
        return notes;
    }

    public int waitForProcess(String lastId, long step, long max, String note) {
        LOGGER.info("Waiting for deployment, {}", note);
        long total = 0;
        while (total < max) {
            total = total + step;
            try {
                Thread.sleep(step);
            } catch (InterruptedException ex) {
            }
            int workItemStatus = dataAccess.getWorkItemStatus(lastId);
            if (workItemStatus > 0) {
                LOGGER.info("Waiting done, status {}, {}", workItemStatus, note);
                return workItemStatus;
            }
        }
        LOGGER.warn("Done waiting, but work items are not done, {}", note);
        return 500;
    }

    private Action getAction(WorkItem workItem) {
        switch (workItem.getType()) {
            case service:
                switch (workItem.getOperation()) {
                    case create:
                        return serviceCreate;
                    case update:
                        return serviceUpdate;
                    case delete:
                        return serviceDelete;
                }
            case module:
                switch (workItem.getOperation()) {
                    case create:
                        return moduleCreate;
                    case update:
                        return moduleUpdate;
                    case delete:
                        return moduleDelete;
                }
            case host:
                switch (workItem.getOperation()) {
                    case create:
                        return hostCreate;
                    case deploy:
                        return hostDeploy;
                    case restart:
                        return hostRestart;
                    case enableVips:
                        return hostVipEnable;
                    case disableVips:
                        return hostVipDisable;
                    case addVips:
                        return hostVipAdd;
                    case removeVips:
                        return hostVipRemove;
                    case smokeTest:
                        return hostSmokeTest;
                    case delete:
                        return hostDelete;
                }
            case vip:
                switch (workItem.getOperation()) {
                    case create:
                        return vipCreate;
                    case update:
                        return vipUpdate;
                    case delete:
                        return vipDelete;
                    case fix:
                        return vipFix;
                }
        }
        throw new RuntimeException("Unknown work item - " + workItem.getType() + " " + workItem.getOperation());
    }

    private void startNext(WorkItem workItem) {
        String nextId = workItem.getNextId();
        if (nextId == null || nextId.isEmpty()) {
            //No more hosts to update in the chain
            if (workItem.getType() == Type.host) {
                dataAccess.updateSatus(
                        workItem.getHost().hostId,
                        false,
                        Const.NO_STATUS);
            }
            return;
        }

        WorkItem nextWorkItem = dataAccess.getWorkItem(nextId);
        if (workItem.getType() == Type.host
                && !workItem.getHost().hostId.equals(nextWorkItem.getHost().hostId)) {
            dataAccess.updateSatus(
                    workItem.getHost().hostId,
                    false,
                    Const.NO_STATUS);
        }
        process(nextWorkItem);
    }

    private void stopNext(WorkItem workItem) {
        String nextId = workItem.getNextId();
        if (nextId == null || nextId.isEmpty()) {
            //No more hosts to update in the chain
            return;
        }

        WorkItem nextWorkItem = dataAccess.getWorkItem(nextId);
        stopNext(nextWorkItem);

        if (workItem.getType() == Type.host
                && !workItem.getHost().hostId.equals(nextWorkItem.getHost().hostId)) {
            dataAccess.updateSatus(
                    nextWorkItem.getHost().hostId,
                    false,
                    "Queued operation cancelled");
        }
        dataAccess.deleteWorkItem(nextId);
        dataAccess.saveWorkItemStatus(nextId, 502);
    }

}
