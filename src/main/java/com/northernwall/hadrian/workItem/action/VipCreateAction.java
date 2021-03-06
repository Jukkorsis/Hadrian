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

import com.northernwall.hadrian.db.SearchSpace;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.Result;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VipCreateAction extends Action {

    private final static Logger LOGGER = LoggerFactory.getLogger(VipCreateAction.class);

    @Override
    public void updateStatus(WorkItem workItem) {
    }

    @Override
    public Result process(WorkItem workItem) {
        LOGGER.info("Creating vip {} for {}", workItem.getVip().dns, workItem.getService().serviceName);
        return Result.success;
    }

    @Override
    public void recordAudit(WorkItem workItem, Result result, Map<String, String> notes, String output) {
        notes.put("Inbound_Protocol", workItem.getVip().inboundProtocol);
        if (workItem.getVip().inboundModifiers != null && !workItem.getVip().inboundModifiers.isEmpty()) {
            String temp = "";
            for (String modifier : workItem.getVip().inboundModifiers) {
                temp = temp + " " + modifier;
            }
            notes.put("Inbound_Modifiers", temp);
        }
        notes.put("Outbound_Protocol", workItem.getVip().outboundProtocol);
        if (workItem.getVip().outboundModifiers != null && !workItem.getVip().outboundModifiers.isEmpty()) {
            String temp = "";
            for (String modifier : workItem.getVip().outboundModifiers) {
                temp = temp + " " + modifier;
            }
            notes.put("Outbound_Modifiers", temp);
        }
        notes.put("DNS", workItem.getVip().dns + "." + workItem.getVip().domain);
        if (workItem.getVip().vipPort > 0) {
            notes.put("VIP_Port", Integer.toString(workItem.getVip().vipPort));
        }
        notes.put("Service_Port", Integer.toString(workItem.getVip().servicePort));
        if (workItem.getVip().httpCheckPort > 0) {
            notes.put("HTTP_Check_Port", Integer.toString(workItem.getVip().httpCheckPort));
        }
        notes.put("External", Boolean.toString(workItem.getVip().external));
        writeAudit(workItem, result, notes, output);
    }

    @Override
    public void success(WorkItem workItem) {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            LOGGER.warn("Could not find vip {} being created", workItem.getVip().vipId);
            return;
        }

        messagingCoodinator.sendMessage("Created new VIP '"
                + workItem.getVip().dns
                + "."
                + workItem.getVip().domain
                + "'.",
                workItem.getTeam().teamId);
    }

    @Override
    public void error(WorkItem workItem) {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            LOGGER.warn("Could not find vip {} being created", workItem.getVip().vipId);
            return;
        }
        LOGGER.warn("Deleting host record due to failure in creating host {]", vip.getVipId());
        dataAccess.deleteVip(vip.getServiceId(), vip.getVipId());
        dataAccess.deleteSearch(
                SearchSpace.vipFqdn,
                vip.getDns() + "." + vip.getDomain());

        messagingCoodinator.sendMessage("VIP '"
                + workItem.getVip().dns
                + "."
                + workItem.getVip().domain
                + "' creation has failed.",
                workItem.getTeam().teamId);
    }

}
