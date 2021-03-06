package com.northernwall.hadrian.handlers.service.helper;

import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.module.ModuleConfigHelper;
import com.northernwall.hadrian.handlers.service.dao.GetVersionData;

public class ReadModuleConfigVersionsRunnable implements Runnable {

    private final ModuleConfigHelper moduleConfigHelper;
    private final Module module;
    private final GetVersionData getVersionData;

    public ReadModuleConfigVersionsRunnable(Module module, GetVersionData getVersionData, ModuleConfigHelper moduleConfigHelper) {
        this.moduleConfigHelper = moduleConfigHelper;
        this.module = module;
        this.getVersionData = getVersionData;
    }

    @Override
    public void run() {
        if (moduleConfigHelper != null
                && module.getConfigName() != null
                && !module.getConfigName().isEmpty()) {
            getVersionData.configVersions.addAll(moduleConfigHelper.readModuleConfigVersions(module));
        }
        getVersionData.configVersions.add(0, Const.NO_CHANGE);
    }

}
