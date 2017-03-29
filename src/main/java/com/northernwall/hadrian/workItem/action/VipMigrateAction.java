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
        if (vip.getMigration() == 1 && workItem.getVip().migration == 2) {
            return migrateStep1(workItem, vip);
        } else if (vip.getMigration() == 2 && workItem.getVip().migration == 3) {
            return migrateStep2(workItem, vip);
        } else if (vip.getMigration() == 3 && workItem.getVip().migration == 2) {
            return rollbackStep2(workItem, vip);
        } else if (vip.getMigration() == 3 && workItem.getVip().migration == 4) {
            return migrateStep3(workItem, vip);
        } else {
            LOGGER.info("Failed to migrating Vip {} for {}, current state {}", workItem.getVip().dns, workItem.getService().serviceName, vip.getMigration());
            return Result.error;
        }
    }

    public Result migrateStep1(WorkItem workItem, Vip vip) {
        LOGGER.info("Migrating Vip {} 1->2 for {}", workItem.getVip().dns, workItem.getService().serviceName);
        vip.setMigration(2);
        dataAccess.saveVip(vip);
        return Result.success;
    }

    public Result migrateStep2(WorkItem workItem, Vip vip) {
        LOGGER.info("Migrating Vip {} 2->3 for {}", workItem.getVip().dns, workItem.getService().serviceName);
        vip.setMigration(3);
        dataAccess.saveVip(vip);
        return Result.success;
    }

    public Result rollbackStep2(WorkItem workItem, Vip vip) {
        LOGGER.info("Rolling back Vip {} 3->2 for {}", workItem.getVip().dns, workItem.getService().serviceName);
        vip.setMigration(2);
        dataAccess.saveVip(vip);
        return Result.success;
    }

    public Result migrateStep3(WorkItem workItem, Vip vip) {
        LOGGER.info("Migrating Vip {} 3->4 for {}", workItem.getVip().dns, workItem.getService().serviceName);
        vip.setMigration(4);
        dataAccess.saveVip(vip);
        return Result.success;
    }

    @Override
    public void success(WorkItem workItem) {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            LOGGER.warn("Could not find vip {} being migrated", workItem.getVip().vipId);
            return;
        }
        vip.setStatus(false, Const.STATUS_NO);
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
