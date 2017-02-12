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

import com.google.gson.Gson;
import com.northernwall.hadrian.ConfigHelper;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.workItem.action.Action;
import com.northernwall.hadrian.workItem.action.HostCreateAction;
import com.northernwall.hadrian.workItem.action.HostDeleteAction;
import com.northernwall.hadrian.workItem.action.HostDeployAction;
import com.northernwall.hadrian.workItem.action.HostRestartAction;
import com.northernwall.hadrian.workItem.action.HostSmokeTestAction;
import com.northernwall.hadrian.workItem.action.HostVipAddAction;
import com.northernwall.hadrian.workItem.action.HostVipDisableAction;
import com.northernwall.hadrian.workItem.action.HostVipEnableAction;
import com.northernwall.hadrian.workItem.action.HostVipRemoveAction;
import com.northernwall.hadrian.workItem.action.ModuleCreateAction;
import com.northernwall.hadrian.workItem.action.ModuleDeleteAction;
import com.northernwall.hadrian.workItem.action.ModuleUpdateAction;
import com.northernwall.hadrian.workItem.action.ServiceCreateAction;
import com.northernwall.hadrian.workItem.action.ServiceDeleteAction;
import com.northernwall.hadrian.workItem.action.ServiceTransferAction;
import com.northernwall.hadrian.workItem.action.ServiceUpdateAction;
import com.northernwall.hadrian.workItem.action.VipCreateAction;
import com.northernwall.hadrian.workItem.action.VipDeleteAction;
import com.northernwall.hadrian.workItem.action.VipMigrateAction;
import com.northernwall.hadrian.workItem.action.VipUpdateAction;
import com.northernwall.hadrian.workItem.dao.CallbackData;
import com.northernwall.hadrian.workItem.helper.SmokeTestHelper;
import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.dshops.metrics.MetricRegistry;
import org.dshops.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WorkItemProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(WorkItemProcessor.class);

    private final Parameters parameters;
    private final ConfigHelper configHelper;
    private final DataAccess dataAccess;
    private final OkHttpClient client;
    private final Gson gson;
    private final SmokeTestHelper smokeTestHelper;
    private final MetricRegistry metricRegistry;
    private final ExecutorService executor;

    public WorkItemProcessor(Parameters parameters, ConfigHelper configHelper, DataAccess dataAccess, OkHttpClient client, Gson gson, SmokeTestHelper smokeTestHelper, MetricRegistry metricRegistry) {
        this.parameters = parameters;
        this.configHelper = configHelper;
        this.dataAccess = dataAccess;
        this.client = client;
        this.gson = gson;
        this.smokeTestHelper = smokeTestHelper;
        this.metricRegistry = metricRegistry;

        //Check if action classes can be constructed
        constructAction("serviceCreate", ServiceCreateAction.class);
        constructAction("serviceUpdate", ServiceUpdateAction.class);
        constructAction("serviceTransfer", ServiceTransferAction.class);
        constructAction("serviceDelete", ServiceDeleteAction.class);

        constructAction("moduleCreate", ModuleCreateAction.class);
        constructAction("moduleUpdate", ModuleUpdateAction.class);
        constructAction("moduleDelete", ModuleDeleteAction.class);

        constructAction("hostCreate", HostCreateAction.class);
        constructAction("hostDeploy", HostDeployAction.class);
        constructAction("hostRestart", HostRestartAction.class);
        constructAction("hostSmokeTest", HostSmokeTestAction.class);
        constructAction("hostDelete", HostDeleteAction.class);

        constructAction("vipCreate", VipCreateAction.class);
        constructAction("vipUpdate", VipUpdateAction.class);
        constructAction("vipDelete", VipDeleteAction.class);
        constructAction("vipMigrate", VipMigrateAction.class);

        constructAction("hostVipEnable", HostVipEnableAction.class);
        constructAction("hostVipDisable", HostVipDisableAction.class);
        constructAction("hostVipAdd", HostVipAddAction.class);
        constructAction("hostVipRemove", HostVipRemoveAction.class);

        executor = Executors.newFixedThreadPool(10);
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
                "action.duration",
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
                action.recordAudit(workItem, result, new HashMap<>(), null);
                LOGGER.info("Work item {} has been successfully processed, no callback expected. {}", action.getName(), workItem.getId());
                dataAccess.deleteWorkItem(workItem.getId());
                dataAccess.saveWorkItemStatus(workItem.getId(), 200);
                startNext(workItem);
                break;
            case error:
                action.error(workItem);
                action.recordAudit(workItem, result, new HashMap<>(), null);
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
                "action.callbackDuration",
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
                action.recordAudit(workItem, result, createNotesFromCallback(callbackData), callbackData.output);
                LOGGER.info("Work item {} has been successfully processed. {}", action.getName(), workItem.getId());
                dataAccess.deleteWorkItem(workItem.getId());
                dataAccess.saveWorkItemStatus(workItem.getId(), 200);
                startNext(workItem);
                break;
            case error:
                action.error(workItem);
                action.recordAudit(workItem, result, createNotesFromCallback(callbackData), callbackData.output);
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
                        return constructAction("serviceCreate", ServiceCreateAction.class);
                    case update:
                        return constructAction("serviceUpdate", ServiceUpdateAction.class);
                    case transfer:
                        return constructAction("serviceTransfer", ServiceUpdateAction.class);
                    case delete:
                        return constructAction("serviceDelete", ServiceDeleteAction.class);
                }
            case module:
                switch (workItem.getOperation()) {
                    case create:
                        return constructAction("moduleCreate", ModuleCreateAction.class);
                    case update:
                        return constructAction("moduleUpdate", ModuleUpdateAction.class);
                    case delete:
                        return constructAction("moduleDelete", ModuleDeleteAction.class);
                }
            case host:
                switch (workItem.getOperation()) {
                    case create:
                        return constructAction("hostCreate", HostCreateAction.class);
                    case deploy:
                        return constructAction("hostDeploy", HostDeployAction.class);
                    case restart:
                        return constructAction("hostRestart", HostRestartAction.class);
                    case enableVips:
                        return constructAction("hostVipEnable", HostVipEnableAction.class);
                    case disableVips:
                        return constructAction("hostVipDisable", HostVipDisableAction.class);
                    case addVips:
                        return constructAction("hostVipAdd", HostVipAddAction.class);
                    case removeVips:
                        return constructAction("hostVipRemove", HostVipRemoveAction.class);
                    case smokeTest:
                        return constructAction("hostSmokeTest", HostSmokeTestAction.class);
                    case delete:
                        return constructAction("hostDelete", HostDeleteAction.class);
                }
            case vip:
                switch (workItem.getOperation()) {
                    case create:
                        return constructAction("vipCreate", VipCreateAction.class);
                    case update:
                        return constructAction("vipUpdate", VipUpdateAction.class);
                    case delete:
                        return constructAction("vipDelete", VipDeleteAction.class);
                    case migrate:
                        return constructAction("vipMigrate", VipMigrateAction.class);
                }
        }
        throw new RuntimeException("Unknown work item - " + workItem.getType() + " " + workItem.getOperation());
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
            action.init(name, dataAccess, parameters, configHelper, client, gson, smokeTestHelper);
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

    private void startNext(WorkItem workItem) {
        String nextId = workItem.getNextId();
        if (nextId == null || nextId.isEmpty()) {
            //No more hosts to update in the chain
            LOGGER.info("Start next {} -> Done", workItem.getId());
            if (workItem.getType() == Type.host) {
                dataAccess.updateSatus(
                        workItem.getHost().hostId,
                        false,
                        Const.NO_STATUS);
            }
            return;
        }

        LOGGER.info("Start next {} -> {}", workItem.getId(), nextId);
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
            LOGGER.info("Stop next {} -> Done", workItem.getId());
            return;
        }

        LOGGER.info("Stop next {} -> {}", workItem.getId(), nextId);
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
