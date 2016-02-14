package com.northernwall.hadrian.service.helper;

import com.northernwall.hadrian.service.dao.GetHostData;
import com.northernwall.hadrian.service.dao.GetModuleData;
import java.io.IOException;

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
        try {
            getHostData.version = infoHelper.readVersion(getHostData.hostName, getModuleData.versionUrl);
        } catch (IOException ex) {
        }
    }
}
