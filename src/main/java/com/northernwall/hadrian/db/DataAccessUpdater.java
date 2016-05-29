package com.northernwall.hadrian.db;

import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Service;
import java.util.HashMap;
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
        
        logger.info("Current DB version is {}, no upgrade required.", version);
        List<Service> services = dataAccess.getAllServices();
        if (services != null && !services.isEmpty()) {
            for (Service service : services) {
                List<Module> modules = dataAccess.getModules(service.getServiceId());
                for (Module module : modules) {
                    if (module.getModuleType() == ModuleType.Deployable) {
                        boolean updated = false;
                        if (module.getNetworkNames() == null) {
                            updated = true;
                            module.setNetworkNames(new HashMap<>());
                        }
                        if (!module.getNetworkNames().containsKey("Prod")) {
                            updated = true;
                            module.getNetworkNames().put("Prod", Boolean.TRUE);
                            System.out.println("not found Prod " + service.getServiceAbbr() + " - " + module.getModuleName());
                        }
                        if (!module.getNetworkNames().containsKey("Test")) {
                            updated = true;
                            module.getNetworkNames().put("Test", Boolean.TRUE);
                            System.out.println("not found Test " + service.getServiceAbbr() + " - " + module.getModuleName());
                        }
                        if (!module.getNetworkNames().containsKey("Reg")) {
                            updated = true;
                            module.getNetworkNames().put("Reg", Boolean.TRUE);
                            System.out.println("not found Reg " + service.getServiceAbbr() + " - " + module.getModuleName());
                        }
                        if (updated) {
                            dataAccess.saveModule(module);
                        }
                    }
                }
            }
        }
    }

    private DataAccessUpdater() {
    }
}
