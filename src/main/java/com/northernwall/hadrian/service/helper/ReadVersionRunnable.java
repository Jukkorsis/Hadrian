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
package com.northernwall.hadrian.service.helper;

import com.northernwall.hadrian.service.dao.GetHostData;
import com.northernwall.hadrian.service.dao.GetModuleData;

public class ReadVersionRunnable implements Runnable {

    private final InfoHelper infoHelper;
    private final GetHostData getHostData;
    private final GetModuleData getModuleData;

    public ReadVersionRunnable(GetHostData getHostData, GetModuleData getModuleData, InfoHelper infoHelper) {
        this.infoHelper = infoHelper;
        this.getHostData = getHostData;
        this.getModuleData = getModuleData;
    }

    @Override
    public void run() {
        getHostData.version = infoHelper.readVersion(getHostData.hostName, getModuleData.versionUrl);
    }
}
