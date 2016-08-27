/*
 * Copyright 2014 Richard Thurston.
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
package com.northernwall.hadrian.handlers.service.helper;

import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.module.ModuleArtifactHelper;
import com.northernwall.hadrian.handlers.service.dao.GetVersionData;
import java.util.List;

public class ReadModuleArtifactVersionsRunnable implements Runnable {

    private final ModuleArtifactHelper moduleArtifactHelper;
    private final Module module;
    private final GetVersionData getVersionData;

    public ReadModuleArtifactVersionsRunnable(Module module, GetVersionData getVersionData, ModuleArtifactHelper moduleArtifactHelper) {
        this.moduleArtifactHelper = moduleArtifactHelper;
        this.module = module;
        this.getVersionData = getVersionData;
    }

    @Override
    public void run() {
        if (moduleArtifactHelper != null
                && module.getMavenGroupId() != null
                && !module.getMavenGroupId().isEmpty()
                && module.getMavenArtifactId() != null
                && !module.getMavenArtifactId().isEmpty()) {
            getVersionData.artifactVersions.addAll(moduleArtifactHelper.readArtifactVersions(module));
        }
        if (getVersionData.artifactVersions.isEmpty()) {
            getVersionData.artifactVersions.add("0.0.0");
        }
    }

}
