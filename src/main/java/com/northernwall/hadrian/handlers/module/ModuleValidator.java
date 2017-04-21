/*
 * Copyright 2017 Richard Thurston.
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
package com.northernwall.hadrian.handlers.module;

import com.northernwall.hadrian.config.Config;
import com.northernwall.hadrian.config.ConfigHelper;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Richard
 */
public class ModuleValidator {
    private final ConfigHelper configHelper;

    public ModuleValidator(ConfigHelper configHelper) {
        this.configHelper = configHelper;
    }
    
    public void checkEnvironmentNames(Map<String, Boolean> environmentNames) throws Http400BadRequestException {
        if (environmentNames == null || environmentNames.isEmpty()) {
            throw new Http400BadRequestException("At least one environment must be selected");
        }

        List<String> falseKeys = null;
        for (Map.Entry<String, Boolean> entry : environmentNames.entrySet()) {
            if (!entry.getValue()) {
                if (falseKeys == null) {
                    falseKeys = new LinkedList<>();
                }
                falseKeys.add(entry.getKey());
            }
        }

        if (falseKeys != null) {
            for (String key : falseKeys) {
                environmentNames.remove(key);
            }
        }

        if (environmentNames.isEmpty()) {
            throw new Http400BadRequestException("At least one environment must be selected");
        }
    }

    public void checkHostAbbr(String hostAbbr) throws Http400BadRequestException {
        if (hostAbbr == null || hostAbbr.isEmpty()) {
            throw new Http400BadRequestException("Host abbr is missing");
        }
        if (!hostAbbr.matches("^[a-zA-Z0-9]+$")) {
            throw new Http400BadRequestException("Host abbr contains an illegal character");
        }
        if (hostAbbr.length() < 3) {
            throw new Http400BadRequestException("Host abbr is to short, minimum is 3");
        }
        if (hostAbbr.length() > 25) {
            throw new Http400BadRequestException("Host abbr is to long, maximum is 25");
        }
        
        Config config = configHelper.getConfig();
        for (String env : config.environmentNames) {
            if (env.equalsIgnoreCase(hostAbbr)) {
                throw new Http400BadRequestException("Host abbr can not be the same as an Env");
            }
        }
        for (String env : config.dataCenters) {
            if (env.equalsIgnoreCase(hostAbbr)) {
                throw new Http400BadRequestException("Host abbr can not be the same as a data center");
            }
        }
    }

    public void checkRange(int value, int min, int max, String text) throws Http400BadRequestException {
        if (value < min) {
            throw new Http400BadRequestException("Requested " + text + " is less than allowed");
        }
        if (value > max) {
            throw new Http400BadRequestException("Requested " + text + " is greater than allowed");
        }
    }

}
