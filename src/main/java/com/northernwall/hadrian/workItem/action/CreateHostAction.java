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
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateHostAction extends Action {

    private final static Logger logger = LoggerFactory.getLogger(CreateHostAction.class);

    public CreateHostAction(DataAccess dataAccess, WorkItemProcessor workItemProcessor) {
        super(dataAccess, workItemProcessor);
    }

    @Override
    protected void success(WorkItem workItem) throws IOException {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            logger.warn("Could not find host {} being created", workItem.getHost().hostId);
            return;
        }
        if (workItem.getNextId() != null) {
            WorkItem nextWorkItem = dataAccess.getWorkItem(workItem.getNextId());
            if (nextWorkItem != null) {
                host.setStatus(true, "Deploying...");
                dataAccess.updateHost(host);
                workItemProcessor.sendWorkItem(nextWorkItem);
            } else {
                logger.warn("Odd, the deploy work item {} for create host {} could not be found", workItem.getNextId(), host.getHostName());
                host.setStatus(false, Const.NO_STATUS);
                dataAccess.updateHost(host);
            }
        } else {
            logger.warn("Odd, create host {} work item has no deploy work item id", host.getHostName());
        }
    }

    @Override
    protected void error(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            logger.warn("Could not find host {} being created", workItem.getHost().hostId);
            return;
        }
        logger.warn("Callback for {} recorded a failure", host.getHostId());
        dataAccess.deleteHost(host);
    }

}
