package com.northernwall.hadrian.db;

import com.northernwall.hadrian.domain.Host;
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
        } else {
            int count = 0;
            List<Service> services = dataAccess.getActiveServices();
            if (services != null && !services.isEmpty()) {
                for (Service service : services) {
                    List<Host> hosts = dataAccess.getHosts(service.getServiceId());
                    if (hosts != null && !hosts.isEmpty()) {
                        for (Host host : hosts) {
                            dataAccess.backfillHostName(host);
                            count++;
                        }
                    }
                }                
            }
            logger.info("Backfilled {} hosts", count);
        }

        logger.info("Current DB version is {}, no upgrade required.", version);
    }

    private DataAccessUpdater() {
    }
}
