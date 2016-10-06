/*
 * Copyright 2014 Richard Thurston.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.northernwall.hadrian.db;

import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAccessUpdater {

    private final static Logger LOGGER = LoggerFactory.getLogger(DataAccessUpdater.class);

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
            LOGGER.info("Backfilled {} hosts", count);
        }

        LOGGER.info("Current DB version is {}, no upgrade required.", version);
    }

    private DataAccessUpdater() {
    }
}
