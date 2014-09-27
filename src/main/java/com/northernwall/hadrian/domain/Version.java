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

package com.northernwall.hadrian.domain;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Richard Thurston
 */
public class Version {
    public String api;
    public String status = "Live";
    public List<Link> links = new LinkedList<>();
    public List<Link> operations = new LinkedList<>();
    public List<ServiceRef> uses = new LinkedList<>();
    public List<ServiceRef> usedby = new LinkedList<>();

    public ServiceRef findUses(String service, String version) {
        if (uses == null || uses.isEmpty()) {
            return null;
        }
        for (ServiceRef ref : uses) {
            if (ref.service.equals(service) && ref.version.equals(version)) {
                return ref;
            }
        }
        return null;
    }
    
    public ServiceRef findUsedBy(String service, String version) {
        if (usedby == null || usedby.isEmpty()) {
            return null;
        }
        for (ServiceRef ref : usedby) {
            if (ref.service.equals(service) && ref.version.equals(version)) {
                return ref;
            }
        }
        return null;
    }
    
    public void addUsedBy(ServiceRef ref) {
        if (usedby == null) {
            usedby = new LinkedList<>();
        }
        usedby.add(ref);
    }

}
