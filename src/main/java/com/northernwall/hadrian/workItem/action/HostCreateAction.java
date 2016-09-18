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

import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.Result;
import com.northernwall.hadrian.workItem.dao.CallbackData;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostCreateAction extends Action {
    
    private final static Logger logger = LoggerFactory.getLogger(HostCreateAction.class);

    @Override
    public Result process(WorkItem workItem) {
        Result result = Result.success;
        recordAudit(workItem, result, null);
        return result;
    }

    @Override
    public Result processCallback(WorkItem workItem, CallbackData callbackData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void recordAudit(WorkItem workItem, Result result, String output) {
        Map<String, String> notes = new HashMap<>();
        notes.put("DC", workItem.getHost().dataCenter);
        notes.put("Network", workItem.getHost().network);
        notes.put("Operating_Env", workItem.getHost().env);
        notes.put("Size_CPU", Integer.toString(workItem.getHost().sizeCpu));
        notes.put("Size_Memory", Integer.toString(workItem.getHost().sizeMemory));
        notes.put("Size_Storage", Integer.toString(workItem.getHost().sizeStorage));
        notes.put("Reason", workItem.getHost().reason);
        recordAudit(workItem, result, notes, output);
    }

    protected void error(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            logger.warn("Could not find host {} being created", workItem.getHost().hostId);
            return;
        }
        logger.warn("Deleting host record due to failure in creating host {]", host.getHostId());
        dataAccess.deleteHost(host);
    }

}
