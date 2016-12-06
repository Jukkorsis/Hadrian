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
package com.northernwall.hadrian.handlers.dashboard.helper;

import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.handlers.dashboard.dao.GetDataCenterData;
import com.northernwall.hadrian.handlers.service.helper.*;

public class ReadAvailabilityRunnable implements Runnable {

    private final GetDataCenterData dataCenterData;
    private final Host host;
    private final Module module;
    private final InfoHelper infoHelper;

    public ReadAvailabilityRunnable(GetDataCenterData dataCenterData, Host host, Module module, InfoHelper infoHelper) {
        this.dataCenterData = dataCenterData;
        this.host = host;
        this.module = module;
        this.infoHelper = infoHelper;
    }

    @Override
    public void run() {
        int temp = infoHelper.readAvailability(host.getHostName(), module.getAvailabilityUrl());
        if (temp >= 200 && temp < 300) {
            dataCenterData.incGood();
        } else if (temp == -1) {
            dataCenterData.incOff();
        } else {
            dataCenterData.incBad();
        }
            
    }
}
