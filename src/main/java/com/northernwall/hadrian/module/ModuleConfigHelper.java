package com.northernwall.hadrian.module;

import java.util.List;

public interface ModuleConfigHelper {

    List<String> readModuleConfigVersions(String configName);

    String getDisplayName();

}
