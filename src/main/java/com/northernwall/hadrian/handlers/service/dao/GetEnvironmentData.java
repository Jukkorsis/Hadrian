package com.northernwall.hadrian.handlers.service.dao;

import com.northernwall.hadrian.handlers.vip.dao.GetVipData;
import com.northernwall.hadrian.handlers.module.dao.GetModuleData;
import com.northernwall.hadrian.handlers.host.dao.GetHostData;
import com.northernwall.hadrian.domain.Module;
import java.util.LinkedList;
import java.util.List;

public class GetEnvironmentData {

    public String environment;
    public List<GetModuleEnvironmentData> modules = new LinkedList<>();

    void addModule(Module module) {
        for (GetModuleEnvironmentData moduleEnvironmentData : modules) {
            if (moduleEnvironmentData.moduleId.equals(module.getModuleId())) {
                return;
            }
        }
        boolean hasSmokeTest = (module.getSmokeTestUrl() != null 
                && !module.getSmokeTestUrl().isEmpty());
        
        GetModuleEnvironmentData moduleEnvironmentData = new GetModuleEnvironmentData(
                module.getModuleId(),
                module.getModuleName(),
                environment,
                hasSmokeTest);
        modules.add(moduleEnvironmentData);
    }

    void addHost(GetHostData hostData, GetModuleData moduleData) {
        for (GetModuleEnvironmentData moduleEnvironmentData : modules) {
            if (moduleEnvironmentData.moduleId.equals(moduleData.moduleId)) {
                moduleEnvironmentData.hosts.add(hostData);
                return;
            }
        }
    }

    void addVip(GetVipData vipData, GetModuleData moduleData) {
        for (GetModuleEnvironmentData moduleEnvironmentData : modules) {
            if (moduleEnvironmentData.moduleId.equals(moduleData.moduleId)) {
                moduleEnvironmentData.vips.add(vipData);
                return;
            }
        }
    }

    void addCustomFunction(GetCustomFunctionData customFunctionData) {
        for (GetModuleEnvironmentData moduleEnvironmentData : modules) {
            if (moduleEnvironmentData.moduleId.equals(customFunctionData.moduleId)) {
                moduleEnvironmentData.cfs.add(customFunctionData);
                return;
            }
        }
    }

}
