package com.northernwall.hadrian.service.helper;

import com.northernwall.hadrian.maven.MavenHelper;
import com.northernwall.hadrian.service.dao.GetModuleData;

public class ReadMavenVersionsRunnable implements Runnable {

    private final MavenHelper mavenHelper;
    private final GetModuleData getModuleData;

    public ReadMavenVersionsRunnable(GetModuleData getModuleData, MavenHelper mavenHelper) {
        this.mavenHelper = mavenHelper;
        this.getModuleData = getModuleData;
    }

    @Override
    public void run() {
        getModuleData.versions.addAll(mavenHelper.readMavenVersions(getModuleData.mavenGroupId, getModuleData.mavenArtifactId));
    }

}
