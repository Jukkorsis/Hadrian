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
package com.northernwall.hadrian;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.domain.Network;
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
    private final ModuleConfigHelper moduleConfigHelper;
    private final AtomicReference<Config> config;

    public ConfigHelper(Parameters parameters, ModuleConfigHelper moduleConfigHelper) {
        this.parameters = parameters;
        this.moduleConfigHelper = moduleConfigHelper;
        this.config = new AtomicReference<>();
        this.config.set(loadConfig());
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
        if (moduleConfigHelper == null) {
            newConfig.moduleConfigName = "Config Name";
        } else {
            newConfig.moduleConfigName = moduleConfigHelper.getDisplayName();
        }

        loadConfig(Const.CONFIG_DATA_CENTERS, Const.CONFIG_DATA_CENTERS_DEFAULT, newConfig.dataCenters);
        loadConfig(Const.CONFIG_ENVS, Const.CONFIG_ENVS_DEFAULT, newConfig.envs);
        loadConfig(Const.CONFIG_PROTOCOLS, Const.CONFIG_PROTOCOLS_DEFAULT, newConfig.protocols);
        loadConfig(Const.CONFIG_DOMAINS, Const.CONFIG_DOMAINS_DEFAULT, newConfig.domains);
        loadConfig(Const.CONFIG_ARTIFACT_TYPES, Const.CONFIG_ARTIFACT_TYPES_DEFAULT, newConfig.artifactTypes);
        loadConfig(Const.CONFIG_SCOPES, Const.CONFIG_SCOPES_DEFAULT, newConfig.scopes);
        loadNetwork(newConfig);

        newConfig.deployableTemplates.add(Const.CONFIG_TEMPLATES_NO_TEMPLATE);
        newConfig.libraryTemplates.add(Const.CONFIG_TEMPLATES_NO_TEMPLATE);
        loadConfig(Const.CONFIG_DEPLOYABLE_TEMPLATES, null, newConfig.deployableTemplates);
        loadConfig(Const.CONFIG_LIBRARY_TEMPLATES, null, newConfig.libraryTemplates);

        newConfig.serviceTypes.add(Const.SERVICE_TYPE_SERVICE);
        newConfig.serviceTypes.add(Const.SERVICE_TYPE_SHARED_LIBRARY);

        LOGGER.info("Config loaded");
        return newConfig;
    }

    private void loadConfig(String key, String defaultValue, List<String> target) {
        String temp = parameters.getString(key, defaultValue);
        if (temp == null) {
            return;
        }
        String[] parts = temp.split(",");
        for (String part : parts) {
            part = part.trim();
            if (!part.isEmpty()) {
                target.add(part);
            }
        }
    }

    private void loadNetwork(Config newConfig) {
        String temp = parameters.getString(Const.CONFIG_NETWORKS, Const.CONFIG_NETWORKS_DEFAULT);
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Network>>(){}.getType();
        newConfig.networks = gson.fromJson(temp, listType);
        for (Network network : newConfig.networks) {
            newConfig.networkNames.add(network.name);
        }
    }

}
