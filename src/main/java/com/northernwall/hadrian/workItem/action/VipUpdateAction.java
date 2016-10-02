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
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.Result;
import com.northernwall.hadrian.workItem.dao.CallbackData;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VipUpdateAction extends Action {

    private final static Logger LOGGER = LoggerFactory.getLogger(VipUpdateAction.class);

    @Override
    public Result process(WorkItem workItem) {
        LOGGER.info("Updating Vip {} for {}", workItem.getVip().dns, workItem.getService().serviceName);
        Result result = Result.success;
        success(workItem);
        recordAudit(workItem, result, null);
        return result;
    }

    @Override
    public Result processCallback(WorkItem workItem, CallbackData callbackData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void recordAudit(WorkItem workItem, Result result, String output) {
        Map<String, String> notes = new HashMap<>();
        notes.put("Protocol", workItem.getVip().protocol);
        notes.put("DNS", workItem.getVip().dns + "." + workItem.getVip().domain);
        notes.put("VIP_Port", Integer.toString(workItem.getVip().vipPort));
        notes.put("Service_Port", Integer.toString(workItem.getVip().servicePort));
        notes.put("External", Boolean.toString(workItem.getVip().external));
        recordAudit(workItem, result, notes, output);
    }

    protected void success(WorkItem workItem) {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            LOGGER.warn("Could not find vip {} being updated", workItem.getVip().vipId);
            return;
        }
        vip.setStatus(Const.NO_STATUS);
        vip.setExternal(workItem.getVip().external);
        vip.setServicePort(workItem.getVip().servicePort);
        vip.setAutoStyle(workItem.getVip().autoStyle);
        dataAccess.updateVip(vip);
    }

    protected void error(WorkItem workItem) {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            LOGGER.warn("Could not find vip {} being updated", workItem.getVip().vipId);
            return;
        }

        //vip.setStatus("Update failed");
        vip.setStatus(Const.NO_STATUS);
        dataAccess.updateVip(vip);
    }

}
