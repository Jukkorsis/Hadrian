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
package com.northernwall.hadrian.workItem.action;

import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.db.SearchSpace;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.Result;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostCreateAction extends Action {

    private final static Logger LOGGER = LoggerFactory.getLogger(HostCreateAction.class);

    @Override
    public void updateStatus(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} being created", workItem.getHost().hostId);
            return;
        }
        dataAccess.updateStatus(
                workItem.getHost().hostId,
                true,
                "Creating...",
                Const.STATUS_WIP);
    }

    @Override
    public Result process(WorkItem workItem) {
        LOGGER.info("Creating host {} for {}", workItem.getHost().hostName, workItem.getService().serviceName);
        return Result.success;
    }

    @Override
    public void recordAudit(WorkItem workItem, Result result, Map<String, String> notes, String output) {
        notes.put("DC", workItem.getHost().dataCenter);
        notes.put("Environment", workItem.getHost().environment);
        notes.put("Size_CPU", Integer.toString(workItem.getMainModule().sizeCpu));
        notes.put("Size_Memory", Integer.toString(workItem.getMainModule().sizeMemory));
        notes.put("Size_Storage", Integer.toString(workItem.getMainModule().sizeStorage));
        if (workItem.getSpecialInstructions() != null
                && !workItem.getSpecialInstructions().isEmpty()) {
            notes.put("Special_Instructions", workItem.getSpecialInstructions());
        }
        if (workItem.getReason() != null
                && !workItem.getReason().isEmpty()) {
            notes.put("Reason", workItem.getReason());
        }
        writeAudit(workItem, result, notes, output);
    }

    @Override
    public void success(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} being created", workItem.getHost().hostId);
            return;
        }

        dataAccess.updateStatus(
                host.getHostId(),
                false,
                Const.STATUS_NO,
                Const.STATUS_NO);

        messagingCoodinator.sendMessage("Created new host '"
                + workItem.getHost().hostName
                + "'.",
                workItem.getTeam().teamId);
    }

    @Override
    public void error(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} being created", workItem.getHost().hostId);
            return;
        }

        LOGGER.warn("Deleting host record due to failure in creating host {]", host.getHostId());
        dataAccess.deleteHost(host);
        dataAccess.deleteSearch(
                SearchSpace.hostName,
                host.getHostName(),
                host.getHostId());

        messagingCoodinator.sendMessage("Creation of host '"
                + workItem.getHost().hostName
                + "' failed.",
                workItem.getTeam().teamId);
    }

}
