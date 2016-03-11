package com.northernwall.hadrian;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.domain.GitMode;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Network;
import com.northernwall.hadrian.parameters.ParameterChangeListener;
import com.northernwall.hadrian.parameters.Parameters;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigHelper implements ParameterChangeListener {
    private final static Logger logger = LoggerFactory.getLogger(ConfigHelper.class);

    private final Parameters parameters;
    private final AtomicReference<Config> config;

    public ConfigHelper(Parameters parameters) {
        this.parameters = parameters;
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
        newConfig.deploymentFolder = parameters.getString(Const.CONFIG_DEPLOYMENT_FOLDER, Const.CONFIG_DEPLOYMENT_FOLDER_DEFAULT);
        newConfig.startCmd = parameters.getString(Const.CONFIG_START_CMD, Const.CONFIG_START_CMD_DEFAULT);
        newConfig.stopCmd = parameters.getString(Const.CONFIG_STOP_CMD, Const.CONFIG_STOP_CMD_DEFAULT);
        newConfig.gitUiURL = parameters.getString(Const.GIT_UI_URL, Const.GIT_UI_URL_DEFAULT);

        loadConfig(Const.CONFIG_DATA_CENTERS, Const.CONFIG_DATA_CENTERS_DEFAULT, newConfig.dataCenters);
        loadConfig(Const.CONFIG_ENVS, Const.CONFIG_ENVS_DEFAULT, newConfig.envs);
        loadConfig(Const.CONFIG_SIZES, Const.CONFIG_SIZES_DEFAULT, newConfig.sizes);
        loadConfig(Const.CONFIG_PROTOCOLS, Const.CONFIG_PROTOCOLS_DEFAULT, newConfig.protocols);
        loadConfig(Const.CONFIG_DOMAINS, Const.CONFIG_DOMAINS_DEFAULT, newConfig.domains);
        loadConfig(Const.CONFIG_ARTIFACT_TYPES, Const.CONFIG_ARTIFACT_TYPES_DEFAULT, newConfig.artifactTypes);
        loadNetwork(newConfig);

        newConfig.deployableTemplates.add(Const.CONFIG_TEMPLATES_NO_TEMPLATE);
        newConfig.libraryTemplates.add(Const.CONFIG_TEMPLATES_NO_TEMPLATE);
        newConfig.testTemplates.add(Const.CONFIG_TEMPLATES_NO_TEMPLATE);
        loadConfig(Const.CONFIG_DEPLOYABLE_TEMPLATES, null, newConfig.deployableTemplates);
        loadConfig(Const.CONFIG_LIBRARY_TEMPLATES, null, newConfig.libraryTemplates);
        loadConfig(Const.CONFIG_TEST_TEMPLATES, null, newConfig.testTemplates);

        newConfig.serviceTypes.add(Const.SERVICE_TYPE_SERVICE);
        newConfig.serviceTypes.add(Const.SERVICE_TYPE_SHARED_LIBRARY);

        newConfig.gitModes.add(GitMode.Consolidated);
        newConfig.gitModes.add(GitMode.Flat);

        newConfig.moduleTypes.add(ModuleType.Deployable);
        newConfig.moduleTypes.add(ModuleType.Library);
        newConfig.moduleTypes.add(ModuleType.Test);
        
        logger.info("Config loaded");
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
            logger.info("Network {} loaded with pattern '{}' and allowUrl '{}'", network.name, network.pattern, network.allowUrl);
        }
    }

}
