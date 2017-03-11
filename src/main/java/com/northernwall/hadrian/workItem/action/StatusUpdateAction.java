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
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.Result;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusUpdateAction extends Action {

    private final static Logger LOGGER = LoggerFactory.getLogger(StatusUpdateAction.class);

    @Override
    public void updateStatus(WorkItem workItem) {
        if (workItem.getHost() != null) {
            Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
            if (host == null) {
                LOGGER.warn("Could not find host {} to update it's status", workItem.getHost().hostId);
                return;
            }
            dataAccess.updateStatus(
                    workItem.getHost().hostId,
                    false,
                    workItem.getReason(),
                    Const.STATUS_INFO);
        } else {
            LOGGER.error("Attempting to update status on unknown entity in workItem {}", workItem.getId());
        }
    }

    @Override
    public Result process(WorkItem workItem) {
        return Result.success;
    }

    @Override
    public void recordAudit(WorkItem workItem, Result result, Map<String, String> notes, String output) {
    }

    @Override
    public void success(WorkItem workItem) {
    }

    @Override
    public void error(WorkItem workItem) {
    }

}
