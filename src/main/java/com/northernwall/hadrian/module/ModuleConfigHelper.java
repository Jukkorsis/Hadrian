package com.northernwall.hadrian.module;

import com.northernwall.hadrian.domain.Module;
import java.util.List;

public interface ModuleConfigHelper {

    List<String> readModuleConfigVersions(Module module);

    String getDisplayName();

}
