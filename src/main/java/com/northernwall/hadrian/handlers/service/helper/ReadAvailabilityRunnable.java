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
package com.northernwall.hadrian.handlers.service.helper;

import com.northernwall.hadrian.handlers.host.dao.GetHostData;
import com.northernwall.hadrian.handlers.module.dao.GetModuleData;

public class ReadAvailabilityRunnable implements Runnable {

    private final InfoHelper infoHelper;
    private final GetHostData getHostData;
    private final GetModuleData getModuleData;

    public ReadAvailabilityRunnable(GetHostData getHostData, GetModuleData getModuleData, InfoHelper infoHelper) {
        this.infoHelper = infoHelper;
        this.getHostData = getHostData;
        this.getModuleData = getModuleData;
    }

    @Override
    public void run() {
        getHostData.availability = infoHelper.readAvailability(getHostData.hostName, getModuleData.availabilityUrl);
    }
}
