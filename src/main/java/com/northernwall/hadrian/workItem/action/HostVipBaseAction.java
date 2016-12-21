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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Richard
 */
public abstract class HostVipBaseAction extends Action {

    private final List<Vip> successVips;
    private Vip failedVip;

    public HostVipBaseAction() {
        successVips = new LinkedList<>();
    }

    protected final Result processVips(WorkItem workItem) {
        List<Vip> vips = dataAccess.getVips(workItem.getService().serviceId);
        if (vips == null || vips.isEmpty()) {
            return Result.success;
        }
        Result result = Result.success;
        for (Vip vip : vips) {
            if (vip.getModuleId().equals(workItem.getMainModule().moduleId)
                    && vip.getEnvironment().equals(workItem.getHost().environment)) {
                result = processVip(workItem, vip);
                if (result == Result.error) {
                    failedVip = vip;
                    dataAccess.updateSatus(
                            workItem.getHost().hostId,
                            false,
                            "Failed to " + getVerb() + " host " + getPreposition() + " VIP");
                    return result;
                }
                successVips.add(vip);
            }
        }
        return result;
    }

    protected abstract Result processVip(WorkItem workItem, Vip vip);

    @Override
    public void recordAudit(WorkItem workItem, Map<String, String> notes, Result result, String output) {
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
        writeAudit(workItem, result, notes, output);
    }

    protected abstract String getVerb();

    protected abstract String getVerbPastTense();

    protected abstract String getPreposition();

}
