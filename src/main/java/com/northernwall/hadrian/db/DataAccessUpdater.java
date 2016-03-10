package com.northernwall.hadrian.db;

import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAccessUpdater {

    private final static Logger logger = LoggerFactory.getLogger(DataAccessUpdater.class);

    public static void update(DataAccess dataAccess) {
        List<Service> services = dataAccess.getServices();
        if (services != null && !services.isEmpty()) {
            for (Service service : services) {
                List<Host> hosts = dataAccess.getHosts(service.getServiceId());
                if (hosts != null && !hosts.isEmpty()) {
                    for (Host host : hosts) {
                        String network = host.getNetwork();
                        if (network.equalsIgnoreCase("prd")) {
                            host.setNetwork("Prod");
                            dataAccess.saveHost(host);
                            logger.info("Updating the network on {} to Prod", host.getHostName());
                        }
                        if (network.equalsIgnoreCase("tst")) {
                            host.setNetwork("Test");
                            dataAccess.saveHost(host);
                            logger.info("Updating the network on {} to Test", host.getHostName());
                        }
                    }
                }
                
            }
        }
    }

    private DataAccessUpdater() {
    }
}
