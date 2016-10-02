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

import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.Result;
import com.northernwall.hadrian.workItem.dao.CallbackData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public class HostVipAddAction extends HostVipBaseAction {

    private final static Logger LOGGER = LoggerFactory.getLogger(HostVipAddAction.class);

    @Override
    public Result process(WorkItem workItem) {
        LOGGER.info("Adding vips for {} {}", workItem.getHost().hostName, workItem.getService().serviceName);
        return Result.success;
    }
    
    @Override
    public Result processCallback(WorkItem workItem, CallbackData callbackData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    protected Result processVip(WorkItem workItem, Vip vip) {
        LOGGER.info("Adding vip {} for {} {}", vip.getDns(), workItem.getHost().hostName, workItem.getService().serviceName);
        return Result.success;
    }

    @Override
    protected String getVerb() {
        return "add";
    }

    @Override
    protected String getVerbPastTense() {
        return "added";
    }

}
