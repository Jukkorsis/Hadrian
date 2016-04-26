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

public class DeploySoftwareAction extends Action {

    private final static Logger logger = LoggerFactory.getLogger(DeploySoftwareAction.class);

    public DeploySoftwareAction(DataAccess dataAccess, WorkItemProcessor workItemProcessor) {
        super(dataAccess, workItemProcessor);
    }

    @Override
    protected void success(WorkItem workItem) throws IOException {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            logger.warn("Could not find host {} being updated", workItem.getHost().hostId);
            return;
        }

        host.setStatus(Const.NO_STATUS);
        dataAccess.updateHost(host);

        if (workItem.getNextId() == null) {
            //No more hosts to update in the chain
            return;
        }

        WorkItem nextWorkItem = dataAccess.getWorkItem(workItem.getNextId());
        Host nextHost = dataAccess.getHost(nextWorkItem.getService().serviceId, nextWorkItem.getHost().hostId);
        if (nextHost == null) {
            logger.error("Finished updating {}, next work item is {}, but could not find it.", workItem.getHost().hostId, nextWorkItem.getHost().hostId);
            return;
        }
        nextHost.setStatus("Deploying...");
        dataAccess.saveHost(nextHost);

        workItemProcessor.sendWorkItem(nextWorkItem);
    }

    @Override
    protected void error(WorkItem workItem) throws IOException {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            logger.warn("Could not find host {} being updated", workItem.getHost().hostId);
            return;
        }

        host.setStatus(Const.NO_STATUS);
        dataAccess.updateHost(host);

        logger.warn("Callback for {} recorded a failure", workItem.getHost().hostId);
    }

}
