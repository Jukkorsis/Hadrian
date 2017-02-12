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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VipMigrateAction extends Action {

    private final static Logger LOGGER = LoggerFactory.getLogger(VipMigrateAction.class);

    @Override
    public Result process(WorkItem workItem) {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            LOGGER.info("Failed the find vip");
            return Result.error;
        }
        switch (vip.getMigration()) {
            case 0:
                LOGGER.info("Migrating Vip {} step 1 for {}", workItem.getVip().dns, workItem.getService().serviceName);
                vip.setMigration(1);
                dataAccess.saveVip(vip);
                return Result.success;
            case 1:
                LOGGER.info("Migrating Vip {} step 2 for {}", workItem.getVip().dns, workItem.getService().serviceName);
                vip.setMigration(2);
                dataAccess.saveVip(vip);
                return Result.success;
            default:
                LOGGER.info("Failed to migrating Vip {} for {}, current state {}", workItem.getVip().dns, workItem.getService().serviceName, vip.getMigration());
                return Result.error;
        }
    }

    @Override
    public void success(WorkItem workItem) {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            LOGGER.warn("Could not find vip {} being migrated", workItem.getVip().vipId);
            return;
        }
        vip.setStatus(false, Const.NO_STATUS);
        dataAccess.updateVip(vip);
    }

    @Override
    public void error(WorkItem workItem) {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            LOGGER.warn("Could not find vip {} being migrated", workItem.getVip().vipId);
            return;
        }

        vip.setStatus(false, "Migration step " + vip.getMigration() + " failed");
        dataAccess.updateVip(vip);
    }

}
