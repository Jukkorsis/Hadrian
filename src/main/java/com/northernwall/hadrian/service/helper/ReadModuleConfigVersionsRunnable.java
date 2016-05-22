package com.northernwall.hadrian.service.helper;

import com.northernwall.hadrian.module.ModuleConfigHelper;
import com.northernwall.hadrian.service.dao.GetModuleData;

public class ReadModuleConfigVersionsRunnable implements Runnable {

    private final ModuleConfigHelper moduleConfigHelper;
    private final GetModuleData getModuleData;

    public ReadModuleConfigVersionsRunnable(GetModuleData getModuleData, ModuleConfigHelper moduleConfigHelper) {
        this.moduleConfigHelper = moduleConfigHelper;
        this.getModuleData = getModuleData;
    }

    @Override
    public void run() {
        if (moduleConfigHelper != null
                && getModuleData.configName != null
                && !getModuleData.configName.isEmpty()) {
            getModuleData.configVersions.addAll(moduleConfigHelper.readModuleConfigVersions(getModuleData.configName));
        }
    }

}
