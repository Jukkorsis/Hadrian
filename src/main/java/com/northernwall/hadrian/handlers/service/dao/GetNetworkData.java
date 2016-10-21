package com.northernwall.hadrian.handlers.service.dao;

import com.northernwall.hadrian.domain.Module;
import java.util.LinkedList;
import java.util.List;

public class GetNetworkData {

    public String network;
    public List<GetModuleNetworkData> modules = new LinkedList<>();

    void addModule(Module module) {
        for (GetModuleNetworkData moduleNetworkData : modules) {
            if (moduleNetworkData.moduleId.equals(module.getModuleId())) {
                return;
            }
        }
        boolean hasSmokeTest = (module.getSmokeTestUrl() != null 
                && !module.getSmokeTestUrl().isEmpty());
        
        GetModuleNetworkData moduleNetworkData = new GetModuleNetworkData(
                module.getModuleId(),
                module.getModuleName(),
                network,
                hasSmokeTest);
        modules.add(moduleNetworkData);
    }

    void addHost(GetHostData hostData, GetModuleData moduleData) {
        for (GetModuleNetworkData moduleNetworkData : modules) {
            if (moduleNetworkData.moduleId.equals(moduleData.moduleId)) {
                moduleNetworkData.hosts.add(hostData);
                return;
            }
        }
    }

    void addVip(GetVipData vipData, GetModuleData moduleData) {
        for (GetModuleNetworkData moduleNetworkData : modules) {
            if (moduleNetworkData.moduleId.equals(moduleData.moduleId)) {
                moduleNetworkData.vips.add(vipData);
                return;
            }
        }
    }

    void addCustomFunction(GetCustomFunctionData customFunctionData) {
        for (GetModuleNetworkData moduleNetworkData : modules) {
            if (moduleNetworkData.moduleId.equals(customFunctionData.moduleId)) {
                moduleNetworkData.cfs.add(customFunctionData);
                return;
            }
        }
    }

}
