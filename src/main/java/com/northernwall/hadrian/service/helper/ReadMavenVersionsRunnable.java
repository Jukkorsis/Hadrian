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

import com.northernwall.hadrian.maven.MavenHelper;
import com.northernwall.hadrian.service.dao.GetModuleData;

public class ReadMavenVersionsRunnable implements Runnable {

    private final MavenHelper mavenHelper;
    private final GetModuleData getModuleData;

    public ReadMavenVersionsRunnable(GetModuleData getModuleData, MavenHelper mavenHelper) {
        this.mavenHelper = mavenHelper;
        this.getModuleData = getModuleData;
    }

    @Override
    public void run() {
        getModuleData.versions.addAll(mavenHelper.readMavenVersions(getModuleData.mavenGroupId, getModuleData.mavenArtifactId));
    }

}
