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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public abstract class HostVipBaseAction extends Action {

    private final static Logger LOGGER = LoggerFactory.getLogger(HostVipBaseAction.class);

    private final List<Vip> successVips;
    private Vip failedVip;

    public HostVipBaseAction() {
        successVips = new LinkedList<>();
    }

    protected final Result processVips(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} who's VIP is being acted upon, {}",
                    workItem.getHost().hostName,
                    workItem.getId());
            return Result.success;
        }

        List<Vip> vips = dataAccess.getVips(workItem.getService().serviceId);
        if (vips == null || vips.isEmpty()) {
            LOGGER.info("Odd, there are no VIPs to process, {}",
                    workItem.getId());
            return Result.success;
        }

            LOGGER.info("Processing potentially {} VIPs, {}",
                    vips.size(),
                    workItem.getId());
        Result result = Result.success;
        for (Vip vip : vips) {
            if (checkVip(workItem, vip, host)) {
            LOGGER.info("Processing {} on {}, {}",
                    host.getHostName(),
                    vip.getDns(),
                    workItem.getId());
                result = processVip(workItem, vip);
                if (result == Result.error) {
                    failedVip = vip;
                    dataAccess.updateStatus(
                            workItem.getHost().hostId,
                            false,
                            "Failed to "
                            + getVerb()
                            + " host "
                            + getPreposition()
                            + " VIP",
                            Const.STATUS_ERROR);
                    return result;
                }
                successVips.add(vip);
            }
        }
        return result;
    }

    private boolean checkVip(WorkItem workItem, Vip vip, Host host) {
        if (workItem.getVip() != null) {
            return workItem.getVip().vipId.equals(vip.getVipId());
        }
        if (!vip.getModuleId().equals(workItem.getMainModule().moduleId)) {
            return false;
        }
        if (!vip.getEnvironment().equals(workItem.getHost().environment)) {
            return false;
        }
        if (vip.getBlackListHosts() != null
                && !vip.getBlackListHosts().isEmpty()
                && vip.getBlackListHosts().contains(host.getHostName())) {
            LOGGER.info("Not processing {} on {} because of blacklist, {}",
                    host.getHostName(),
                    vip.getDns(),
                    workItem.getId());

            return false;
        }
        return true;
    }

    protected abstract Result processVip(WorkItem workItem, Vip vip);

    @Override
    public void recordAudit(WorkItem workItem, Result result, Map<String, String> notes, String output) {
        if (successVips == null
                || successVips.isEmpty()) {
            if (failedVip == null) {
                output = "No VIPs to "
                        + getVerb();
            } else {
                output = "Failed to "
                        + getVerb()
                        + " host "
                        + getPreposition()
                        + " VIP "
                        + failedVip.getDns();
            }
        } else {
            output = "VIPs successfully "
                    + getVerbPastTense() + ":\n";
            for (Vip vip : successVips) {
                output = output
                        + " - "
                        + vip.getDns()
                        + "\n";
            }
            if (failedVip != null) {
                output = "Failed to "
                        + getVerb()
                        + " host "
                        + getPreposition()
                        + " VIP "
                        + failedVip.getDns();
            }
        }
        writeAudit(workItem, result, notes, output);
    }

    @Override
    public void error(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} that is being acted upon", workItem.getHost().hostId);
            return;
        }
        dataAccess.updateStatus(
                workItem.getHost().hostId,
                false,
                "Failed to " + getVerb() + " host " + getPreposition() + " VIP %% ago",
                Const.STATUS_ERROR);
    }

    protected abstract String getVerb();

    protected abstract String getVerbPastTense();

    protected abstract String getPreposition();

}
