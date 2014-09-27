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

package com.northernwall.hadrian;

import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceRef;
import com.northernwall.hadrian.domain.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class WarningProcessor {
    private final static Logger logger = LoggerFactory.getLogger(WarningProcessor.class);

    private final DataAccess dataAccess;

    public WarningProcessor(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void scanServices() {
    }

    private void generateWarningsForUsesRef(ServiceRef serviceRef) {
        Service usedByService = dataAccess.getService(serviceRef.service);

        if (usedByService == null) {
            logger.error("Could not find service {}", serviceRef.service);
            return;
        }

        for (Version usedByVersion : usedByService.versions) {
            if (usedByVersion.api.equals(serviceRef.version)) {
                //serviceRef.retireWarnings = (usedByVersion.status.equals("Retired") || usedByVersion.status.equals("Retiring"));
            }
        }
    }

    private void generateWarningsForUsedBy(Service service, Version version) {
        if (version.usedby != null && !version.usedby.isEmpty()) {
            for (ServiceRef usedByServiceRef : version.usedby) {
                Service usedByService = dataAccess.getService(usedByServiceRef.service);
                if (usedByService != null) {
                    Version usedByVersion = usedByService.findVersion(usedByServiceRef.version);
                    if (usedByVersion != null) {
                        ServiceRef serviceRef = usedByVersion.findUses(service.getId(), version.api);
                        if (serviceRef != null) {
                            //serviceRef.retireWarnings = (version.status.equals("Retired") || version.status.equals("Retiring"));
                            dataAccess.save(usedByService);
                        }
                    }
                }
            }
        }
    }

}
