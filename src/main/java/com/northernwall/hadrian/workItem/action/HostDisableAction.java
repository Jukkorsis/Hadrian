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

import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.Result;
import com.northernwall.hadrian.workItem.dao.CallbackData;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostDisableAction extends Action {

    private final static Logger LOGGER = LoggerFactory.getLogger(HostDisableAction.class);

    @Override
    public Result process(WorkItem workItem) {
        return Result.success;
        //return disableVips(workItem);
    }
    
    @Override
    public Result processCallback(WorkItem workItem, CallbackData callbackData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected Result disableVips(WorkItem workItem) {
        LOGGER.info("Disabling vips for {} {}", workItem.getHost().hostName, workItem.getService().serviceName);
        List<Vip> vips = dataAccess.getVips(workItem.getService().serviceId);
        if (vips == null || vips.isEmpty()) {
            return Result.success;
        }
        Result result = Result.success;
        List<Vip> successVips = new LinkedList<>();
        for (Vip vip : vips) {
            if (vip.getAutoStyle().equals("Auto")) {
                result = disableVip(workItem, vip);
                if (result == Result.error) {
                    recordAudit(workItem, result, successVips, vip);
                    return result;
                }
                successVips.add(vip);
            }
        }
        
        recordAudit(workItem, result, successVips, null);
        return result;
    }

    protected Result disableVip(WorkItem workItem, Vip vip) {
        LOGGER.info("Disabling vip {} for {} {}", vip.getDns(), workItem.getHost().hostName, workItem.getService().serviceName);
        return Result.success;
    }

    protected void recordAudit(WorkItem workItem, Result result, List<Vip> successVips, Vip failedVip) {
        String output = null;
        if (successVips == null || successVips.isEmpty()) {
            if (failedVip == null) {
                output = "No VIPs to disable";
            } else {
                output = "Failed to disable VIP " + failedVip.getDns();
            }
        } else {
            output = "VIPs successfully disabled:\n";
            for (Vip vip : successVips) {
                output = output + " - " + vip.getDns() +"\n";
            }
            if (failedVip != null) {
                output = "Failed to disable VIP " + failedVip.getDns();
            }
        }
        recordAudit(workItem, result, null, output);
    }

}
