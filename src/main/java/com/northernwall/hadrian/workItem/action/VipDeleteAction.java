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
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.Result;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VipDeleteAction extends Action {

    private final static Logger LOGGER = LoggerFactory.getLogger(VipDeleteAction.class);

    @Override
    public Result process(WorkItem workItem) {
        LOGGER.info("Deleting Vip {} for {}", workItem.getVip().dns, workItem.getService().serviceName);
        return Result.success;
    }

    @Override
    public void recordAudit(WorkItem workItem, Result result, Map<String, String> notes, String output) {
        notes.put("Reason", workItem.getReason());
        writeAudit(workItem, result, notes, output);
    }

    @Override
    public void success(WorkItem workItem) {
        dataAccess.deleteVip(workItem.getService().serviceId, workItem.getVip().vipId);
        dataAccess.deleteSearch(
                SearchSpace.vipFqdn,
                workItem.getVip().dns + "." + workItem.getVip().domain);

        messagingCoodinator.sendMessage("Vip '"
                + workItem.getVip().dns
                + "' has been decommissioned.",
                workItem.getTeam().teamId);
    }

    @Override
    public void error(WorkItem workItem) {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            LOGGER.warn("Could not find vip {} being deleted", workItem.getVip().vipId);
            return;
        }

        dataAccess.updateStatus(
                vip.getVipId(),
                false,
                "Delete failed %% ago",
                Const.STATUS_ERROR);

        messagingCoodinator.sendMessage("Failed to decommission vip '"
                + workItem.getVip().dns
                + "'.",
                workItem.getTeam().teamId);
    }

}
