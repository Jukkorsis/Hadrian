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
package com.northernwall.hadrian.handlers.vip;

import com.northernwall.hadrian.config.Config;
import com.northernwall.hadrian.config.ConfigHelper;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import com.northernwall.hadrian.handlers.vip.dao.PostVipData;

/**
 *
 * @author Richard
 */
public class VipValidator {
    private final ConfigHelper configHelper;

    public VipValidator(ConfigHelper configHelper) {
        this.configHelper = configHelper;
    }
    
    public String checkVipName(PostVipData data) throws Http400BadRequestException {
        String dns = data.dns;
        if (dns == null || dns.isEmpty()) {
            throw new Http400BadRequestException("VIP name is missing");
        }
        dns = dns.trim();
        if (dns == null || dns.isEmpty()) {
            throw new Http400BadRequestException("VIP name is missing");
        }
        
        if (!dns.matches("^[a-zA-Z0-9/-]+$")) {
            throw new Http400BadRequestException("VIP name contains an illegal character");
        }
        
        Config config = configHelper.getConfig();
        for (String dataCenter : config.dataCenters) {
            if (dns.endsWith("-" + dataCenter)) {
                throw new Http400BadRequestException("VIP name can not end in -" + dataCenter);
            }
        }
        
        if (dns.length() < 2) {
            throw new Http400BadRequestException("VIP name is to short, minimum is 2");
        }
        
        if (dns.length() > 30) {
            throw new Http400BadRequestException("VIP name is to long, maximum is 30");
        }
        return dns;
    }

}
