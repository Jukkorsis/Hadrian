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
package com.northernwall.hadrian.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.northernwall.hadrian.domain.Environment;
import com.northernwall.hadrian.domain.InboundProtocol;
import com.northernwall.hadrian.module.ModuleConfigHelper;
import com.northernwall.hadrian.parameters.ParameterChangeListener;
import com.northernwall.hadrian.parameters.Parameters;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigHelper implements ParameterChangeListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigHelper.class);

    private final Parameters parameters;
    private final Gson gson;
    private final ModuleConfigHelper moduleConfigHelper;
    private final AtomicReference<Config> config;

    public ConfigHelper(Parameters parameters, Gson gson, ModuleConfigHelper moduleConfigHelper) {
        this.parameters = parameters;
        this.gson = gson;
        this.moduleConfigHelper = moduleConfigHelper;
        this.config = new AtomicReference<>();
        this.config.set(loadConfig());

        parameters.registerChangeListener(this);
    }

    public Config getConfig() {
        return config.get();
    }

    @Override
    public void onChange(List<String> keys) {
        this.config.set(loadConfig());
    }

    private Config loadConfig() {
        Config newConfig = new Config();

        newConfig.mavenGroupId = parameters.getString(Const.CONFIG_MAVEN_GROUP_ID, Const.CONFIG_MAVEN_GROUP_ID_DEFAULT);
        newConfig.versionUrl = parameters.getString(Const.CONFIG_VERSION_URL, Const.CONFIG_VERSION_URL_DEFAULT);
        newConfig.availabilityUrl = parameters.getString(Const.CONFIG_AVAILABILITY_URL, Const.CONFIG_AVAILABILITY_URL_DEFAULT);
        newConfig.smokeTestUrl = parameters.getString(Const.CONFIG_SMOKE_TEST_URL, Const.CONFIG_SMOKE_TEST_URL_DEFAULT);
        newConfig.deploymentFolder = parameters.getString(Const.CONFIG_DEPLOYMENT_FOLDER, Const.CONFIG_DEPLOYMENT_FOLDER_DEFAULT);
        newConfig.dataFolder = parameters.getString(Const.CONFIG_DATA_FOLDER, Const.CONFIG_DATA_FOLDER_DEFAULT);
        newConfig.logsFolder = parameters.getString(Const.CONFIG_LOGS_FOLDER, Const.CONFIG_LOGS_FOLDER_DEFAULT);
        newConfig.gitUiURL = parameters.getString(Const.GIT_UI_URL, Const.GIT_UI_URL_DEFAULT);
        newConfig.securityGroupName = parameters.getString(Const.CONFIG_SECURITY_GROUP_NAME, Const.CONFIG_SECURITY_GROUP_NAME_DEFAULT);
        newConfig.minCpu = parameters.getInt(Const.CONFIG_MIN_CPU, Const.CONFIG_MIN_CPU_DEFAULT);
        newConfig.maxCpu = parameters.getInt(Const.CONFIG_MAX_CPU, Const.CONFIG_MAX_CPU_DEFAULT);
        newConfig.minMemory = parameters.getInt(Const.CONFIG_MIN_MEMORY, Const.CONFIG_MIN_MEMORY_DEFAULT);
        newConfig.maxMemory = parameters.getInt(Const.CONFIG_MAX_MEMORY, Const.CONFIG_MAX_MEMORY_DEFAULT);
        newConfig.minStorage = parameters.getInt(Const.CONFIG_MIN_STORAGE, Const.CONFIG_MIN_STORAGE_DEFAULT);
        newConfig.maxStorage = parameters.getInt(Const.CONFIG_MAX_STORAGE, Const.CONFIG_MAX_STORAGE_DEFAULT);
        newConfig.hostSpecialInstructions = parameters.getString(Const.CONFIG_HOST_SPECIAL_INSTRUCTIONS, Const.CONFIG_HOST_SPECIAL_INSTRUCTIONS_DEFAULT);
        newConfig.hostSpecialInstructionsTrueSla = parameters.getString(Const.CONFIG_HOST_SPECIAL_INSTRUCTIONS_TRUE_SLA, Const.CONFIG_HOST_SPECIAL_INSTRUCTIONS_TRUE_SLA_DEFAULT);
        newConfig.hostSpecialInstructionsFalseSla = parameters.getString(Const.CONFIG_HOST_SPECIAL_INSTRUCTIONS_FALSE_SLA, Const.CONFIG_HOST_SPECIAL_INSTRUCTIONS_FALSE_SLA_DEFAULT);
        if (moduleConfigHelper == null) {
            newConfig.moduleConfigName = "Config Name";
        } else {
            newConfig.moduleConfigName = moduleConfigHelper.getDisplayName();
        }
        newConfig.enableHostProvisioning = parameters.getBoolean(Const.CONFIG_ENABLE_HOST_PROVISIONING, Const.CONFIG_ENABLE_HOST_PROVISIONING_DEFAULT);
        newConfig.enableHostReboot = parameters.getBoolean(Const.CONFIG_ENABLE_HOST_REBOOT, Const.CONFIG_ENABLE_HOST_REBOOT_DEFAULT);
        newConfig.enableVipProvisioning = parameters.getBoolean(Const.CONFIG_ENABLE_VIP_PROVISIONING, Const.CONFIG_ENABLE_VIP_PROVISIONING_DEFAULT);
        newConfig.enableVipMigration = parameters.getBoolean(Const.CONFIG_ENABLE_VIP_MIGRATION, Const.CONFIG_ENABLE_VIP_MIGRATION_DEFAULT);
        newConfig.enableSshAccess = parameters.getBoolean(Const.CONFIG_ENABLE_SSH_ACCESS, Const.CONFIG_ENABLE_SSH_ACCESS_DEFAULT);

        loadConfig(Const.CONFIG_DATA_CENTERS, Const.CONFIG_DATA_CENTERS_DEFAULT, newConfig.dataCenters);
        loadConfig(Const.CONFIG_PLATFORMS, Const.CONFIG_PLATFORMS_DEFAULT, newConfig.platforms);
        loadConfig(Const.CONFIG_PRIORITY_MODES, Const.CONFIG_PRIORITY_MODES_DEFAULT, newConfig.priorityModes);
        loadConfig(Const.CONFIG_DOMAINS, Const.CONFIG_DOMAINS_DEFAULT, newConfig.domains);
        loadConfig(Const.CONFIG_ARTIFACT_TYPES, Const.CONFIG_ARTIFACT_TYPES_DEFAULT, newConfig.artifactTypes);
        loadConfig(Const.CONFIG_SCOPES, Const.CONFIG_SCOPES_DEFAULT, newConfig.scopes);
        loadInboundProtocol(newConfig);
        loadEnvironment(newConfig);

        loadFolderWhiteList(parameters.getString(Const.CONFIG_FOLDER_WHITE_LIST, null), newConfig.folderWhiteList);

        LOGGER.info("Config loaded, {}", gson.toJson(newConfig));
        return newConfig;
    }

    private void loadConfig(String key, String defaultValue, List<String> target) {
        String param = parameters.getString(key, defaultValue);
        if (param == null) {
            return;
        }
        String[] parts = param.split(",");
        for (String part : parts) {
            String temp = part.trim();
            if (!temp.isEmpty()) {
                target.add(temp);
            }
        }
    }

    private void loadInboundProtocol(Config newConfig) {
        String temp = parameters.getString(Const.CONFIG_PROTOCOL_MODES, Const.CONFIG_PROTOCOL_MODES_DEFAULT);
        Type listType = new TypeToken<ArrayList<InboundProtocol>>() {
        }.getType();
        newConfig.inboundProtocols = gson.fromJson(temp, listType);
    }

    private void loadEnvironment(Config newConfig) {
        String temp = parameters.getString(Const.CONFIG_ENVIRONMENTS, Const.CONFIG_ENVIRONMENTS_DEFAULT);
        Type listType = new TypeToken<ArrayList<Environment>>() {
        }.getType();
        newConfig.environments = gson.fromJson(temp, listType);
        for (Environment environment : newConfig.environments) {
            newConfig.environmentNames.add(environment.name);
        }
    }

    private void loadFolderWhiteList(String temp, List<String> folderWhiteList) {
        if (temp == null || temp.isEmpty()) {
            return;
        }
        String[] folders = temp.split(",");
        for (String folder : folders) {
            String tempFolder = folder.trim();
            if (!tempFolder.isEmpty() && !tempFolder.equals("/")) {
                if (!tempFolder.startsWith("/")) {
                    tempFolder = "/" + tempFolder;
                }
                if (!tempFolder.endsWith("/") && tempFolder.length() > 1) {
                    tempFolder = tempFolder + "/";
                }
                folderWhiteList.add(tempFolder);
            }
        }
    }

}
