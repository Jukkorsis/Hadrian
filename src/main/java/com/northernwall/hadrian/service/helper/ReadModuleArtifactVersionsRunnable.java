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
package com.northernwall.hadrian.service.helper;

import com.northernwall.hadrian.module.ModuleArtifactHelper;
import com.northernwall.hadrian.service.dao.GetModuleData;

public class ReadModuleArtifactVersionsRunnable implements Runnable {

    private final ModuleArtifactHelper moduleArtifactHelper;
    private final GetModuleData getModuleData;

    public ReadModuleArtifactVersionsRunnable(GetModuleData getModuleData, ModuleArtifactHelper moduleArtifactHelper) {
        this.moduleArtifactHelper = moduleArtifactHelper;
        this.getModuleData = getModuleData;
    }

    @Override
    public void run() {
        if (moduleArtifactHelper != null
                && getModuleData.mavenGroupId != null
                && !getModuleData.mavenGroupId.isEmpty()
                && getModuleData.mavenArtifactId != null
                && !getModuleData.mavenArtifactId.isEmpty()) {
            getModuleData.versions.addAll(moduleArtifactHelper.readMavenVersions(getModuleData.mavenGroupId, getModuleData.mavenArtifactId));
        }
    }

}
