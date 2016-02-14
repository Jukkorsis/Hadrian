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
package com.northernwall.hadrian.workItem.dao;

import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Team;

public class ModuleData {
    public String moduleId;
    public String moduleName;
    public String moduleType;
    public String template;
    public String gitRepo;
    public String gitPath;
    public String gitFolder;
    public String mavenGroupId;
    public String mavenArtifactId;
    public String artifactType;
    public String artifactSuffix;

    public static ModuleData create(Team team, Module module) {
        if (module == null) {
            return null;
        }
        ModuleData temp = new ModuleData();
        temp.moduleId = module.getModuleId();
        temp.moduleName = module.getModuleName();
        temp.moduleType = module.getModuleType();
        temp.gitRepo = team.getGitRepo();
        temp.gitPath = module.getGitPath();
        temp.gitFolder = module.getGitFolder();
        temp.mavenGroupId = module.getMavenGroupId();
        temp.mavenArtifactId = module.getMavenArtifactId();
        temp.artifactType = module.getArtifactType();
        temp.artifactSuffix = module.getArtifactSuffix();
        return temp;
    }

}
