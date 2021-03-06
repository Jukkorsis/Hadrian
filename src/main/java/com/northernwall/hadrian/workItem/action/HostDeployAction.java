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
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.Result;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostDeployAction extends Action {

    private final static Logger LOGGER = LoggerFactory.getLogger(HostDeployAction.class);

    @Override
    public void updateStatus(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} being deployed too", workItem.getHost().hostId);
            return;
        }
        dataAccess.updateStatus(
                workItem.getHost().hostId,
                true,
                "Deploying...",
                Const.STATUS_WIP);
    }

    @Override
    public Result process(WorkItem workItem) {
        LOGGER.info("Deploying to host {} of {}", workItem.getHost().hostName, workItem.getService().serviceName);
        return Result.success;
    }

    @Override
    public void recordAudit(WorkItem workItem, Result result, Map<String, String> notes, String output) {
        if (workItem.getHost().version != null) {
            notes.put("Version", workItem.getHost().version);
        }
        if (workItem.getHost().prevVersion != null) {
            notes.put("Prev Version", workItem.getHost().prevVersion);
        }
        if (workItem.getHost().versionUrl != null) {
            notes.put("Version Url", workItem.getHost().versionUrl);
        }
        if (workItem.getHost().configVersion != null) {
            notes.put("Config_Version", workItem.getHost().configVersion);
        }
        notes.put("Reason", workItem.getReason());
        writeAudit(workItem, result, notes, output);
    }

    @Override
    public void success(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} being deployed too", workItem.getHost().hostId);
            return;
        }

        if (workItem.getHost().version == null) {
            messagingCoodinator.sendMessage("Deployment to host '"
                    + workItem.getHost().hostName
                    + "' successful.",
                    workItem.getTeam().teamId);
        } else {
            messagingCoodinator.sendMessage("Deployed "
                    + workItem.getHost().version
                    + " to host '"
                    + workItem.getHost().hostName
                    + "'.",
                    workItem.getTeam().teamId);
        }
    }

    @Override
    public void error(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} being deployed too", workItem.getHost().hostId);
            return;
        }

        dataAccess.updateStatus(
                workItem.getHost().hostId,
                false,
                "Deploy failed %% ago",
                Const.STATUS_ERROR);
        
            messagingCoodinator.sendMessage("Deployment to host '"
                    + workItem.getHost().hostName
                    + "' failed.",
                    workItem.getTeam().teamId);
    }

}
