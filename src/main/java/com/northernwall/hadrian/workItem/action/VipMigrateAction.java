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
import com.northernwall.hadrian.workItem.dao.CallbackData;
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
        } else if (vip.getMigration() == 2 && workItem.getVip().migration == 2) {
            return rollbackStep2(workItem, vip);
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
        vip.getMigratedDCs().clear();
        vip.getUnmigratedDCs().addAll(configHelper.getConfig().dataCenters);
        dataAccess.saveVip(vip);
        return Result.success;
    }

    public Result migrateStep2(WorkItem workItem, Vip vip) {
        LOGGER.info("Migrating Vip {} 2->3 for {}", workItem.getVip().dns, workItem.getService().serviceName);
        for (String dc : workItem.getVip().migrateDCs) {
            if (vip.getMigratedDCs().contains(dc)) {
                LOGGER.warn("DC {} selected for migration, but already migrated", dc);
            } else {
                vip.getMigratedDCs().add(dc);
                vip.getUnmigratedDCs().remove(dc);
            }
        }
        if (vip.getUnmigratedDCs().isEmpty()) {
            vip.setMigration(3);
        }
        dataAccess.saveVip(vip);
        return Result.success;
    }

    public Result rollbackStep2(WorkItem workItem, Vip vip) {
        LOGGER.info("Rolling back Vip {} {}->2 for {}", workItem.getVip().dns, vip.getMigration(), workItem.getService().serviceName);
        vip.setMigration(2);
        vip.getMigratedDCs().clear();
        vip.getUnmigratedDCs().clear();
        vip.getUnmigratedDCs().addAll(configHelper.getConfig().dataCenters);
        dataAccess.saveVip(vip);
        return Result.success;
    }

    public Result migrateStep3(WorkItem workItem, Vip vip) {
        LOGGER.info("Migrating Vip {} 3->4 for {}", workItem.getVip().dns, workItem.getService().serviceName);
        vip.setMigration(4);
        vip.getMigratedDCs().clear();
        vip.getUnmigratedDCs().clear();
        dataAccess.saveVip(vip);
        return Result.success;
    }

    @Override
    public Result processCallback(WorkItem workItem, CallbackData callbackData) {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            LOGGER.info("Failed the find vip");
            return Result.error;
        }
        if (vip.getMigration() == 1 && workItem.getVip().migration == 2) {
            return migrateStep1Callback(workItem, vip, callbackData);
        } else if (vip.getMigration() == 2 && workItem.getVip().migration == 3) {
            return migrateStep2Callback(workItem, vip, callbackData);
        } else if (vip.getMigration() == 2 && workItem.getVip().migration == 2) {
            return rollbackStep2Callback(workItem, vip, callbackData);
        } else if (vip.getMigration() == 3 && workItem.getVip().migration == 2) {
            return rollbackStep2Callback(workItem, vip, callbackData);
        } else if (vip.getMigration() == 3 && workItem.getVip().migration == 4) {
            return migrateStep3Callback(workItem, vip, callbackData);
        } else {
            LOGGER.info("Failed to migrating Vip {} for {}, current state {}", workItem.getVip().dns, workItem.getService().serviceName, vip.getMigration());
            return Result.error;
        }
    }

    public Result migrateStep1Callback(WorkItem workItem, Vip vip, CallbackData callbackData) {
        LOGGER.info("Migrating Vip {} 1->2 for {}", workItem.getVip().dns, workItem.getService().serviceName);
        vip.setMigration(2);
        dataAccess.saveVip(vip);
        return Result.success;
    }

    public Result migrateStep2Callback(WorkItem workItem, Vip vip, CallbackData callbackData) {
        LOGGER.info("Migrating Vip {} 2->3 for {}", workItem.getVip().dns, workItem.getService().serviceName);
        for (String dc : workItem.getVip().migrateDCs) {
            if (vip.getMigratedDCs().contains(dc)) {
                LOGGER.warn("DC {} selected for migration, but already migrated", dc);
            } else {
                vip.getMigratedDCs().add(dc);
                vip.getUnmigratedDCs().remove(dc);
            }
        }
        if (vip.getUnmigratedDCs().isEmpty()) {
            vip.setMigration(3);
        }
        dataAccess.saveVip(vip);
        return Result.success;
    }

    public Result rollbackStep2Callback(WorkItem workItem, Vip vip, CallbackData callbackData) {
        LOGGER.info("Rolling back Vip {} {}->2 for {}", workItem.getVip().dns, vip.getMigration(), workItem.getService().serviceName);
        vip.setMigration(2);
        vip.getMigratedDCs().clear();
        vip.getUnmigratedDCs().clear();
        vip.getUnmigratedDCs().addAll(configHelper.getConfig().dataCenters);
        dataAccess.saveVip(vip);
        return Result.success;
    }

    public Result migrateStep3Callback(WorkItem workItem, Vip vip, CallbackData callbackData) {
        LOGGER.info("Migrating Vip {} 3->4 for {}", workItem.getVip().dns, workItem.getService().serviceName);
        vip.setMigration(4);
        vip.getMigratedDCs().clear();
        vip.getUnmigratedDCs().clear();
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
        switch (vip.getMigration()) {
            case 2:
                messagingCoodinator.sendMessage("VIP '"
                        + workItem.getVip().dns
                        + "."
                        + workItem.getVip().domain
                        + "' migration to step 2 was successful. The F5s have been configured, requests are being processed by the A10s",
                        workItem.getTeam().teamId);
                break;
            case 3:
                messagingCoodinator.sendMessage("VIP '"
                        + workItem.getVip().dns
                        + "."
                        + workItem.getVip().domain
                        + "' migration to step 3 was successful. The F5s have been configured and are processing requests",
                        workItem.getTeam().teamId);
                break;
            case 4:
                messagingCoodinator.sendMessage("VIP '"
                        + workItem.getVip().dns
                        + "."
                        + workItem.getVip().domain
                        + "' migration has now been completed",
                        workItem.getTeam().teamId);
                break;
        }
    }

    @Override
    public void error(WorkItem workItem) {
        Vip vip = dataAccess.getVip(workItem.getService().serviceId, workItem.getVip().vipId);
        if (vip == null) {
            LOGGER.warn("Could not find vip {} being migrated", workItem.getVip().vipId);
            return;
        }

        dataAccess.updateStatus(
                vip.getVipId(),
                false,
                "Migration step " + vip.getMigration() + " failed %% ago",
                Const.STATUS_ERROR);

        messagingCoodinator.sendMessage("VIP '"
                + workItem.getVip().dns
                + "."
                + workItem.getVip().domain
                + "' migration step failed. Contact PST and Ops.",
                workItem.getTeam().teamId);
    }

}
