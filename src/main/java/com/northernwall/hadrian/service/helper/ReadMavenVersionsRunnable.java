package com.northernwall.hadrian.service.helper;

import com.northernwall.hadrian.maven.MavenHelper;
import com.northernwall.hadrian.service.dao.GetServiceData;

public class ReadMavenVersionsRunnable implements Runnable {

    private final MavenHelper mavenHelper;
    private final GetServiceData getServiceData;

    public ReadMavenVersionsRunnable(GetServiceData getServiceData, MavenHelper mavenHelper) {
        this.mavenHelper = mavenHelper;
        this.getServiceData = getServiceData;
    }

    @Override
    public void run() {
        getServiceData.versions.addAll(mavenHelper.readMavenVersions(getServiceData.mavenGroupId, getServiceData.mavenArtifactId));
    }

}
