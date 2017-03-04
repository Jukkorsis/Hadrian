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

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.Result;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostRebootAction extends Action {
    public static final String REBOOTING = "Rebooting...";
    public static final String REBOOT_QUEUED = "Reboot Queued";
    public static final String REBOOT_FAILED = "Last reboot failed";

    private final static Logger LOGGER = LoggerFactory.getLogger(HostRebootAction.class);

    @Override
    public void updateStatus(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} being rebooted", workItem.getHost().hostId);
            return;
        }
        if (!host.getStatus().equals(REBOOTING)) {
            dataAccess.updateSatus(
                    workItem.getHost().hostId,
                    true,
                    REBOOTING);
        }
    }

    @Override
    public Result process(WorkItem workItem) {
        LOGGER.info("Rebooting host {} of {}", workItem.getHost().hostName, workItem.getService().serviceName);
        return Result.success;
    }

    @Override
    public void recordAudit(WorkItem workItem, Result result, Map<String, String> notes, String output) {
        notes.put("Reason", workItem.getReason());
        writeAudit(workItem, result, notes, output);
    }

    @Override
    public void success(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} being rebooted", workItem.getHost().hostId);
            return;
        }
        dataAccess.updateSatus(
                workItem.getHost().hostId,
                false,
                Const.NO_STATUS);
    }

    @Override
    public void error(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} being rebooted", workItem.getHost().hostId);
            return;
        }

        dataAccess.updateSatus(
                workItem.getHost().hostId,
                false,
                REBOOT_FAILED);
    }

}
