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

import com.google.gson.Gson;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.workItem.Result;
import com.northernwall.hadrian.workItem.dao.CallbackData;
import com.northernwall.hadrian.workItem.dao.SmokeTestData;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostSmokeTestAction extends Action {

    private final static Logger LOGGER = LoggerFactory.getLogger(HostSmokeTestAction.class);
    
    public static SmokeTestData ExecuteSmokeTest(String smokeTestUrl, String endPoint, Parameters parameters, Gson gson, OkHttpClient client) {
        if (smokeTestUrl == null || smokeTestUrl.isEmpty() || endPoint == null || endPoint.isEmpty()) {
            return null;
        }
        
        LOGGER.info("Smoke testing EP {} with {}", endPoint, smokeTestUrl);
        
        String url = Const.HTTP + smokeTestUrl.replace(Const.END_POINT, endPoint);
        try {
            Request.Builder builder = new Request.Builder().url(url);
            if (parameters.getUsername() != null
                    && parameters.getUsername().isEmpty()
                    && parameters.getPassword() != null
                    && parameters.getPassword().isEmpty()) {
                builder.addHeader(
                        "Authorization",
                        Credentials.basic(parameters.getUsername(), parameters.getPassword()));
            }
            Request request = builder.build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                try (InputStream stream = response.body().byteStream()) {
                    Reader reader = new InputStreamReader(stream);
                    return gson.fromJson(reader, SmokeTestData.class);
                }
            } else {
                LOGGER.warn("Call to {} failed with code {}", url, response.code());
                return null;
            }
        } catch (IOException ex) {
            LOGGER.warn("Call to {} failed with exception {}", url, ex.getMessage());
            return null;
        }
    }

    @Override
    public Result process(WorkItem workItem) {
        String smokeTestUrl = workItem.getMainModule().smokeTestUrl;
        if (smokeTestUrl == null || smokeTestUrl.isEmpty()) {
            return Result.success;
        }

        SmokeTestData smokeTestData = ExecuteSmokeTest(
                smokeTestUrl,
                workItem.getHost().hostName,
                parameters,
                gson,
                client);

        Result result;
        String output = null;
        if (smokeTestData == null) {
            result = Result.error;
        } else if (!smokeTestData.result.equalsIgnoreCase("PASS")) {
            result = Result.error;
            output = smokeTestData.output;
        } else {
            result = Result.success;
            output = smokeTestData.output;
        }

        recordAudit(workItem, result, output);
        return result;
    }

    @Override
    public Result processCallback(WorkItem workItem, CallbackData callbackData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void recordAudit(WorkItem workItem, Result result, String output) {
        recordAudit(workItem, result, null, output);
    }

    protected void error(WorkItem workItem) {
        Host host = dataAccess.getHost(workItem.getService().serviceId, workItem.getHost().hostId);
        if (host == null) {
            LOGGER.warn("Could not find host {} being deployed too", workItem.getHost().hostId);
            return;
        }

        host.setStatus(false, "Last deployment failed");
        dataAccess.updateHost(host);
    }

}
