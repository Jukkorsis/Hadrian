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
import com.northernwall.hadrian.workItem.dao.CallbackData;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostDeleteAction extends Action {

    private final static Logger LOGGER = LoggerFactory.getLogger(HostDeleteAction.class);

    @Override
    public void updateStatus(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} being deleted", workItem.getHost().hostId);
            return;
        }
        dataAccess.updateStatus(
                workItem.getHost().hostId,
                true,
                "Deleting...",
                Const.STATUS_WIP);
    }

    @Override
    public Result process(WorkItem workItem) {
        String hostname = workItem.getHost().hostName;
        checkIfHostExists(hostname);
        LOGGER.info("Deleting host {} for {}", hostname, workItem.getService().serviceName);
        return Result.success;
    }
    
    protected boolean checkIfHostExists(String hostname) {
        if (parameters.getBoolean(Const.CHECK_RESOLVE_HOSTNAME, Const.CHECK_RESOLVE_HOSTNAME_DEFAULT)) {
            try {
                InetAddress address = InetAddress.getByName(hostname);
                LOGGER.info("Deleting host {} with IP address {}", hostname, address.getHostAddress());
            } catch (UnknownHostException ex) {
                LOGGER.warn("Could not find host {} to delete, IP address does not resolve", hostname);
                return false;
            }
        }
        return true;
    }

    @Override
    public Result processCallback(WorkItem workItem, CallbackData callbackData) {
        throw new UnsupportedOperationException("Not supported yet.");
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
            LOGGER.warn("Could not find host {} being delete.", workItem.getHost().hostId);
            return;
        }
        
        dataAccess.deleteHost(host);
        dataAccess.deleteSearch(
                SearchSpace.hostName, 
                host.getHostName(),
                host.getHostId());

        messagingCoodinator.sendMessage("Host '"
                + workItem.getHost().hostName
                + "' has been decommissioned.",
                workItem.getTeam().teamId);
    }

    @Override
    public void error(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} being delete.", workItem.getHost().hostId);
            return;
        }
        
        dataAccess.updateStatus(
                host.getHostId(),
                false,
                "Delete failed %% ago",
                Const.STATUS_ERROR);

        messagingCoodinator.sendMessage("Failed to decommission host '"
                + workItem.getHost().hostName
                + "'.",
                workItem.getTeam().teamId);
    }

}
