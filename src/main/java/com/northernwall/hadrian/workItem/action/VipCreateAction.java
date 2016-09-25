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

public class VipCreateAction extends Action {

    private final static Logger LOGGER = LoggerFactory.getLogger(VipCreateAction.class);

    @Override
    public Result process(WorkItem workItem) {
        LOGGER.info("Creating vip {} for {}", workItem.getVip().dns, workItem.getService().serviceName);
        Result result = Result.success;
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
        recordAudit(workItem, result, notes, null);
    }

    protected void success(WorkItem workItem) {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            LOGGER.warn("Could not find vip {} being created", workItem.getVip().vipId);
            return;
        }

        vip.setStatus(Const.NO_STATUS);
        dataAccess.updateVip(vip);
    }

    protected void error(WorkItem workItem) {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            LOGGER.warn("Could not find vip {} being created", workItem.getVip().vipId);
            return;
        }
        LOGGER.warn("Deleting host record due to failure in creating host {]", vip.getVipId());
        dataAccess.deleteVip(vip.getServiceId(), vip.getVipId());
    }

}