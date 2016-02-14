package com.northernwall.hadrian.service.helper;

import com.northernwall.hadrian.service.dao.GetHostData;
import com.northernwall.hadrian.service.dao.GetModuleData;
import java.io.IOException;

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
        try {
            getHostData.availability = infoHelper.readAvailability(getHostData.hostName, getModuleData.availabilityUrl);
        } catch (IOException ex) {
        }
    }
}
