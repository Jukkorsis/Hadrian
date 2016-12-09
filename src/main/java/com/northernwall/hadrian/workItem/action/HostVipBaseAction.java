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

import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.Result;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Richard
 */
public abstract class HostVipBaseAction extends Action {

    protected final Result processVips(WorkItem workItem) {
        List<Vip> vips = dataAccess.getVips(workItem.getService().serviceId);
        if (vips == null || vips.isEmpty()) {
            return Result.success;
        }
        Result result = Result.success;
        List<Vip> successVips = new LinkedList<>();
        for (Vip vip : vips) {
            if (vip.getModuleId().equals(workItem.getMainModule().moduleId)
                    && vip.getEnvironment().equals(workItem.getHost().environment)) {
                result = processVip(workItem, vip);
                if (result == Result.error) {
                    updateStatusFailure(workItem);
                    recordAudit(workItem, result, successVips, vip);
                    return result;
                }
                successVips.add(vip);
            }
        }

        recordAudit(workItem, result, successVips, null);
        return result;
    }

    protected abstract Result processVip(WorkItem workItem, Vip vip);

    private void updateStatusFailure(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            return;
        }
        dataAccess.updateSatus(
                workItem.getHost().hostId,
                false,
                "Failed to " + getVerb() + " host " + getPreposition() + " VIP");
    }

    private void recordAudit(WorkItem workItem, Result result, List<Vip> successVips, Vip failedVip) {
        String output = null;
        if (successVips == null || successVips.isEmpty()) {
            if (failedVip == null) {
                output = "No VIPs to " + getVerb();
            } else {
                output = "Failed to " + getVerb() + " host " + getPreposition() + " VIP " + failedVip.getDns();
            }
        } else {
            output = "VIPs successfully " + getVerbPastTense() + ":\n";
            for (Vip vip : successVips) {
                output = output + " - " + vip.getDns() + "\n";
            }
            if (failedVip != null) {
                output = "Failed to " + getVerb() + " host " + getPreposition() + " VIP " + failedVip.getDns();
            }
        }
        writeAudit(workItem, result, null, output);
    }

    protected abstract String getVerb();

    protected abstract String getVerbPastTense();

    protected abstract String getPreposition();

}
