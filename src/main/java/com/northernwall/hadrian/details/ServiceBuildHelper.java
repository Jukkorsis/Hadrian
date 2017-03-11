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
package com.northernwall.hadrian.details;

import com.google.gson.Gson;
import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.GMT;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.parameters.Parameters;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public class ServiceBuildHelper {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServiceBuildHelper.class);

    private final DataAccess dataAccess;
    private final OkHttpClient client;
    private final Parameters parameters;
    private final Gson gson;

    public ServiceBuildHelper(DataAccess dataAccess, OkHttpClient client, Parameters parameters, Gson gson) {
        this.dataAccess = dataAccess;
        this.client = client;
        this.parameters = parameters;
        this.gson = gson;
    }

    public void triggerBuild(Team team, Service service, String branch, User user) {
        if (team == null 
                || service == null
                || branch == null
                || branch.isEmpty()) {
            return;
        }
        
        String url = parameters.getString(Const.SERVICE_BUILD_URL, null);
        if (url == null || url.isEmpty()) {
            return;
        }

        String username = parameters.getString(Const.SERVICE_BUILD_USERNAME, null);
        String password = parameters.getString(Const.SERVICE_BUILD_PASSWORD, null);

        ServiceBuildData serviceBuildData = new ServiceBuildData();
        serviceBuildData.group = team.getGitGroup();
        serviceBuildData.project = service.getGitProject();
        serviceBuildData.branch = branch;

        Request.Builder builder = new Request.Builder()
                .url(url);

        if (username != null
                && !username.isEmpty()
                && password != null
                && !password.isEmpty()) {
            builder = builder.addHeader("Authorization", Credentials.basic(username, password));
        }

        RequestBody body = RequestBody.create(Const.JSON_MEDIA_TYPE, gson.toJson(serviceBuildData));

        Request httpRequest = builder.post(body).build();

        try {
            Response resp = client.newCall(httpRequest).execute();
            
            if (resp.isSuccessful()) {
                LOGGER.warn("Build triggered to {} with code {}", url, resp.code());
                
                Map<String, String> notes = new HashMap<>();
                notes.put("Reason", "Manually requested build");
                notes.put("Source Branch",branch);
                
                Audit audit = new Audit();
                audit.serviceId = service.getServiceId();
                audit.setTimePerformed(GMT.getGmtAsDate());
                audit.timeRequested = GMT.getGmtAsDate();
                audit.requestor = user.getUsername();
                audit.type = Type.service;
                audit.operation = Operation.build;
                audit.successfull = true;
                audit.notes = gson.toJson(notes);
                
                dataAccess.saveAudit(audit, null);
            } else {
                LOGGER.warn("Build not triggered {}, code {}", url, resp.code());
            }
        } catch (Exception ex) {
            LOGGER.warn("Error while triggering build {}, error {}", url, ex.getMessage());
        }
    }

}
