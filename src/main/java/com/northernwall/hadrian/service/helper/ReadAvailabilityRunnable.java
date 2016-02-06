package com.northernwall.hadrian.service.helper;

import com.northernwall.hadrian.service.dao.GetHostData;
import com.northernwall.hadrian.service.dao.GetServiceData;
import java.io.IOException;

public class ReadAvailabilityRunnable implements Runnable {

    private final InfoHelper infoHelper;
    private final GetHostData getHostData;
    private final GetServiceData getServiceData;

    public ReadAvailabilityRunnable(GetHostData getHostData, GetServiceData getServiceData, InfoHelper infoHelper) {
        this.infoHelper = infoHelper;
        this.getHostData = getHostData;
        this.getServiceData = getServiceData;
    }

    @Override
    public void run() {
        try {
            getHostData.availability = infoHelper.readAvailability(getHostData.hostName, getServiceData.availabilityUrl);
        } catch (IOException ex) {
        }
    }
}
