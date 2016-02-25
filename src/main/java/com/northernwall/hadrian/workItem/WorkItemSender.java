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
package com.northernwall.hadrian.workItem;

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.parameters.Parameters;
import java.io.IOException;

/**
 *
 * @author Richard Thurston
 */
public abstract class WorkItemSender {

    private final Parameters parameters;

    public WorkItemSender(Parameters parameters) {
        this.parameters = parameters;
    }

    public abstract boolean sendWorkItem(WorkItem workItem) throws IOException;

    protected String getGitUrl(WorkItem workItem) {
        String gitUrl = parameters.getString(Const.GIT_PATH_URL, Const.GIT_PATH_URL_DETAULT);
        gitUrl = gitUrl.replace(Const.GIT_PATH_PATTERN_REPO, workItem.getTeam().gitRepo);
        gitUrl = gitUrl.replace(Const.GIT_PATH_PATTERN_PROJECT, workItem.getMainModule().gitProject);
        return gitUrl;
    }

}
