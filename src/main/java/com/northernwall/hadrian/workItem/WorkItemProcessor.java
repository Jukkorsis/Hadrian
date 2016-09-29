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
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.workItem.action.HostDisableAction;
import com.northernwall.hadrian.workItem.action.HostEnableAction;
import com.northernwall.hadrian.workItem.action.VipCreateAction;
import com.northernwall.hadrian.workItem.action.VipDeleteAction;
import com.northernwall.hadrian.workItem.action.VipUpdateAction;
import com.northernwall.hadrian.workItem.dao.CallbackData;
import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkItemProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(WorkItemProcessor.class);

    private final DataAccess dataAccess;
    private final Gson gson;
    private final Action moduleCreate;
    private final Action moduleUpdate;
    private final Action moduleDelete;
    private final Action hostCreate;
    private final Action hostDeploy;
    private final Action hostRestart;
    private final Action hostDelete;
    private final Action hostDisable;
    private final Action hostEnable;
    private final Action vipCreate;
    private final Action vipUpdate;
    private final Action vipDelete;

    public WorkItemProcessor(Parameters parameters, DataAccess dataAccess, OkHttpClient client, Gson gson) {
        this.dataAccess = dataAccess;
        this.gson = gson;
        moduleCreate = constructAction("moduleCreate", ModuleCreateAction.class, parameters, client);
        moduleUpdate = constructAction("moduleUpdate", ModuleUpdateAction.class, parameters, client);
        moduleDelete = constructAction("moduleDelete", ModuleDeleteAction.class, parameters, client);
        
        hostCreate = constructAction("hostCreate", HostCreateAction.class, parameters, client);
        hostDeploy = constructAction("hostDeploy", HostDeployAction.class, parameters, client);
        hostRestart = constructAction("hostRestart", HostRestartAction.class, parameters, client);
        hostDisable = constructAction("hostDisable", HostDisableAction.class, parameters, client);
        hostEnable = constructAction("hostEnable", HostEnableAction.class, parameters, client);
        hostDelete = constructAction("hostDelete", HostDeleteAction.class, parameters, client);
        
        vipCreate = constructAction("vipCreate", VipCreateAction.class, parameters, client);
        vipUpdate = constructAction("vipUpdate", VipUpdateAction.class, parameters, client);
        vipDelete = constructAction("vipDelete", VipDeleteAction.class, parameters, client);
    }

    private Action constructAction(String name, Class defaultClass, Parameters parameters, OkHttpClient client) {
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
            action.init(dataAccess, parameters, client, gson);
            LOGGER.info("Constructed action {} with {}", name, factoryName);
            return action;
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Could not build Action, could not find class " + factoryName);
        } catch (InstantiationException ex) {
            throw new RuntimeException("Could not build Action, could not instantiation class " + factoryName);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Could not build Action, could not access class " + factoryName);
        }
    }

    public void processWorkItem(WorkItem workItem) throws IOException {
        if (workItem == null) {
            return;
        }
        workItem.setNextId(null);
        dataAccess.saveWorkItem(workItem);
        process(workItem);
    }

    public void processWorkItems(List<WorkItem> workItems) throws IOException {
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

        process(workItems.get(0));
    }

    private void process(WorkItem workItem) {
        Action action = getAction(workItem);
        Result result = action.process(workItem);

        switch (result) {
            case success:
                LOGGER.info("Work item {} has been successfully processed, no callback expected.", workItem.getId());
                dataAccess.deleteWorkItem(workItem.getId());
                dataAccess.saveWorkItemStatus(workItem.getId(), 200);
                startNext(workItem);
                break;
            case error:
                LOGGER.warn("Work item {} failed to be process, no callback expected.", workItem.getId());
                dataAccess.deleteWorkItem(workItem.getId());
                dataAccess.saveWorkItemStatus(workItem.getId(), 502);
                stopNext(workItem);
                break;
            case wip:
                LOGGER.info("Work item {} is being processed, waiting for callback.", workItem.getId());
        }
    }

    public void processCallback(CallbackData callbackData) throws IOException {
        WorkItem workItem = dataAccess.getWorkItem(callbackData.requestId);
        Action action = getAction(workItem);
        Result result = action.processCallback(workItem, callbackData);

        switch (result) {
            case success:
                LOGGER.info("Work item {} has been successfully processed.", workItem.getId());
                dataAccess.deleteWorkItem(workItem.getId());
                dataAccess.saveWorkItemStatus(workItem.getId(), 200);
                startNext(workItem);
                break;
            case error:
                LOGGER.warn("Work item {} failed to be process.", workItem.getId());
                dataAccess.deleteWorkItem(workItem.getId());
                dataAccess.saveWorkItemStatus(workItem.getId(), 502);
                stopNext(workItem);
                break;
            case wip:
                LOGGER.info("Work item {} is still being processed.", workItem.getId());
                return;
        }

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
                    case disableVips:
                        return hostDisable;
                    case enableVips:
                        return hostEnable;
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
                }
        }
        throw new RuntimeException("Unknown work item - " + workItem.getType() + " " + workItem.getOperation());
    }

    private void startNext(WorkItem workItem) {
        String nextId = workItem.getNextId();
        if (nextId == null || nextId.isEmpty()) {
            //No more hosts to update in the chain
            return;
        }

        WorkItem nextWorkItem = dataAccess.getWorkItem(nextId);
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

        if (nextWorkItem.getType() == Type.host) {
            Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
            if (host != null) {
                host.setStatus(false, "Last operation cancelled");
                dataAccess.updateHost(host);
            }
        }
        dataAccess.deleteWorkItem(nextId);
        dataAccess.saveWorkItemStatus(nextId, 502);
    }

}
