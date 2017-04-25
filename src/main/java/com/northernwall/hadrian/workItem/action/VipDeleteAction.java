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
    public void success(WorkItem workItem) {
        dataAccess.deleteVip(workItem.getService().serviceId, workItem.getVip().vipId);
        dataAccess.deleteSearch(
                SearchSpace.vipFqdn,
                workItem.getVip().dns + "." + workItem.getVip().domain,
                workItem.getVip().vipId);
    }

    @Override
    public void error(WorkItem workItem) {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            LOGGER.warn("Could not find vip {} being deleted", workItem.getVip().vipId);
            return;
        }

        vip.setStatus(false, "Delete failed");
        dataAccess.updateVip(vip);
    }

}
