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
import com.northernwall.hadrian.workItem.dao.SmokeTestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostSmokeTestAction extends Action {

    private final static Logger LOGGER = LoggerFactory.getLogger(HostSmokeTestAction.class);

    @Override
    public void updateStatus(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} being smoke tested", workItem.getHost().hostId);
            return;
        }
        dataAccess.updateSatus(
                workItem.getHost().hostId,
                true,
                "Smoke Testing...");
    }

    @Override
    public Result process(WorkItem workItem) {
        String smokeTestUrl = workItem.getMainModule().smokeTestUrl;
        if (smokeTestUrl == null || smokeTestUrl.isEmpty()) {
            return Result.success;
        }

        SmokeTestData smokeTestData = smokeTestHelper.ExecuteSmokeTest(
                smokeTestUrl,
                workItem.getHost().hostName);

        Result result;
        String output = null;
        if (smokeTestData == null) {
            result = Result.error;
        } else if (smokeTestData.result == null
                || smokeTestData.result.isEmpty()
                ||!smokeTestData.result.equalsIgnoreCase("PASS")) {
            result = Result.error;
            output = smokeTestData.output;
        } else {
            result = Result.success;
            output = smokeTestData.output;
        }

        writeAudit(workItem, result, null, output);
        return result;
    }

    @Override
    public void success(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} being smoke tested", workItem.getHost().hostId);
            return;
        }

        dataAccess.updateSatus(
                host.getHostId(),
                false,
                Const.NO_STATUS);
    }

    @Override
    public void error(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} being smoke tested", workItem.getHost().hostId);
            return;
        }

        dataAccess.updateSatus(
                workItem.getHost().hostId,
                false,
                "Last smoke test failed");
    }

}
