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
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.Result;
import com.northernwall.hadrian.workItem.dao.CallbackData;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostDeleteAction extends Action {

    private final static Logger LOGGER = LoggerFactory.getLogger(HostDeleteAction.class);

    @Override
    public Result process(WorkItem workItem) {
        Result result = Result.success;
        success(workItem);
        recordAudit(workItem, null, result, null);
        return result;
    }

    @Override
    public Result processCallback(WorkItem workItem, CallbackData callbackData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void recordAudit(WorkItem workItem, CallbackData callbackData, Result result, String output) {
        Map<String, String> notes = createNotesFromCallback(callbackData);
        notes.put("Reason", workItem.getReason());
        recordAudit(workItem, result, notes, output);
    }

    protected void success(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} being delete.", workItem.getHost().hostId);
            return;
        }
        
        dataAccess.deleteHost(host);
        dataAccess.deleteSearch(
                Const.SEARCH_SPACE_HOST_NAME, 
                host.getHostName());
    }

    protected void error(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} being delete.", workItem.getHost().hostId);
            return;
        }
        
        dataAccess.updateSatus(
                host.getHostId(),
                false,
                "Delete host failed");
    }

}
