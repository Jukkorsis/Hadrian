package com.northernwall.hadrian.db;

import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Service;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAccessUpdater {

    private final static Logger logger = LoggerFactory.getLogger(DataAccessUpdater.class);

    public static void update(DataAccess dataAccess) {
        String version = dataAccess.getVersion();

        if (version == null) {
            dataAccess.setVersion("1.5");
            update(dataAccess);
        }
        
        List<Service> services = dataAccess.getActiveServices();
        if (services != null && !services.isEmpty()) {
            for (Service service : services) {
                List<Module> modules = dataAccess.getModules(service.getServiceId());
                if (modules != null && !modules.isEmpty()) {
                    for (Module module : modules) {
                        if (module.getModuleType() == ModuleType.Deployable ||
                                module.getModuleType() == ModuleType.Simulator) {
                            if (module.getNetworkNames().containsKey("Test")) {
                                boolean value = module.getNetworkNames().get("Test").booleanValue();
                                logger.info("Found a deployable or simulator with a Test network, {} in {} with value {}", module.getModuleName(), service.getServiceName(), value);
                                module.getNetworkNames().put("Sandbox", value);
                                module.getNetworkNames().remove("Test");
                                dataAccess.saveModule(module);
                            }
                        }
                    }
                }
            }
        }
        
        logger.info("Current DB version is {}, no upgrade required.", version);
    }

    private DataAccessUpdater() {
    }
}
