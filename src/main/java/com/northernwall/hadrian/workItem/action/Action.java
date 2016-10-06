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

import com.google.gson.Gson;
import com.northernwall.hadrian.ConfigHelper;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.GMT;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.workItem.Result;
import com.northernwall.hadrian.workItem.dao.CallbackData;
import com.squareup.okhttp.OkHttpClient;
import java.util.Map;

/**
 *
 * @author rthursto
 */
public abstract class Action {

    private String name;
    protected DataAccess dataAccess;
    protected Parameters parameters;
    protected ConfigHelper configHelper;
    protected OkHttpClient client;
    protected Gson gson;

    public Action() {
    }

    public final void init(String name, DataAccess dataAccess, Parameters parameters, ConfigHelper configHelper, OkHttpClient client, Gson gson) {
        this.name = name;
        this.dataAccess = dataAccess;
        this.parameters = parameters;
        this.configHelper = configHelper;
        this.client = client;
        this.gson = gson;
    }

    public String getName() {
        return name;
    }

    public abstract Result process(WorkItem workItem);

    public abstract Result processCallback(WorkItem workItem, CallbackData callbackData);

    protected void recordAudit(WorkItem workItem, Result result, Map<String, String> notes, String output) {
        Audit audit = new Audit();
        audit.serviceId = workItem.getService().serviceId;
        audit.timePerformed = GMT.getGmtAsDate();
        audit.timeRequested = workItem.getRequestDate();
        audit.requestor = workItem.getUsername();
        audit.type = workItem.getType();
        audit.operation = workItem.getOperation();
        audit.successfull = (result == Result.success);
        if (workItem.getMainModule() != null) {
            audit.moduleName = workItem.getMainModule().moduleName;
        }
        if (workItem.getHost() != null) {
            audit.hostName = workItem.getHost().hostName;
        }
        if (workItem.getVip() != null) {
            audit.vipName = workItem.getVip().dns;
        }
        if (notes == null || notes.isEmpty()) {
            audit.notes = "";
        } else {
            audit.notes = gson.toJson(notes);
        }
        dataAccess.saveAudit(audit, output);
    }

    protected String getGitUrl(WorkItem workItem) {
        String gitUrl = parameters.getString(Const.GIT_PATH_URL, Const.GIT_PATH_URL_DETAULT);
        gitUrl = gitUrl.replace(Const.GIT_PATH_PATTERN_GROUP, workItem.getTeam().gitGroup);
        gitUrl = gitUrl.replace(Const.GIT_PATH_PATTERN_PROJECT, workItem.getMainModule().gitProject);
        return gitUrl;
    }

}
