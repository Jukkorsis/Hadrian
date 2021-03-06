/*
 * Copyright 2016 Richard Thurston.
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
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public class HostVipRemoveAction extends HostVipBaseAction {

    private final static Logger LOGGER = LoggerFactory.getLogger(HostVipRemoveAction.class);

    @Override
    public void updateStatus(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} being removed from vip", workItem.getHost().hostId);
            return;
        }
        dataAccess.updateStatus(
                workItem.getHost().hostId,
                true,
                "Removing from VIP...",
                Const.STATUS_WIP);
    }

    @Override
    public Result process(WorkItem workItem) {
        return processVips(workItem);
    }
    
    @Override
    protected Result processVip(WorkItem workItem, Vip vip) {
        LOGGER.info("Removing vip {} for {} {}", vip.getDns(), workItem.getHost().hostName, workItem.getService().serviceName);
        return Result.success;
    }

    @Override
    protected String getVerb() {
        return "remove";
    }

    @Override
    protected String getVerbPastTense() {
        return "removed";
    }

    @Override
    protected String getPreposition() {
        return "from";
    }

}
