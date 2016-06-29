package com.northernwall.hadrian.db;

import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Service;
import java.util.List;
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
                        if (module.getModuleName().toLowerCase().contains("simulator") 
                                && module.getModuleType().equals(ModuleType.Deployable)) {
                            logger.info("Found a sim, {} in {}", module.getModuleName(), service.getServiceName());
                            module.setModuleType(ModuleType.Simulator);
                            dataAccess.saveModule(module);
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
