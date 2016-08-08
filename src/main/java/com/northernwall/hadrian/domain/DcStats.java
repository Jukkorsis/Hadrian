/*
 * Copyright 2015 Richard Thurston.
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

package com.northernwall.hadrian.domain;

/**
 *
 * @author Richard Thurston
 */
public class DcStats {
    private double deployableHosts;
    private double deployableHostsRate;
    private double simulatorHosts;
    private double simulatorHostsRate;
    private double allHostsPerService;
    private double deployableHostsPerDeployableModules;
    private double simulatorHostsPerSimulatorModules;
    private double deployableCpu;
    private double deployableCpuRate;
    private double deployableCpusPerDeployableHosts;
    private double deployableCpusPerDeployableModules;

    public DcStats(Stats stats, 
            double deployableHosts, double deployableHostsRate, 
            double simulatorHosts, double simulatorHostsRate, 
            double deployableCpu, double deployableCpuRate) {
        this.deployableHosts = deployableHosts;
        this.deployableHostsRate = deployableHostsRate;
        this.simulatorHosts = simulatorHosts;
        this.simulatorHostsRate = simulatorHostsRate;
        this.deployableHostsPerDeployableModules = deployableHosts/stats.getDeployableModules();
        this.simulatorHostsPerSimulatorModules = simulatorHosts/stats.getSimulatorModules();
        this.deployableCpu = deployableCpu;
        this.deployableCpuRate = deployableCpuRate;
        this.allHostsPerService = (deployableHosts+simulatorHosts)/stats.getServices();
        this.deployableCpusPerDeployableHosts = deployableCpu/deployableHosts;
        this.deployableCpusPerDeployableModules = deployableCpu/stats.getDeployableModules();
    }

    DcStats(Stats stats, DcStats baseDcStats, int addWeeks) {
        deployableHosts = Math.round(baseDcStats.deployableHosts + (baseDcStats.deployableHostsRate*addWeeks));
        deployableHostsRate = baseDcStats.deployableHostsRate;
        simulatorHosts = Math.round(baseDcStats.simulatorHosts + (baseDcStats.simulatorHostsRate*addWeeks));
        simulatorHostsRate = baseDcStats.simulatorHostsRate;
        deployableHostsPerDeployableModules = deployableHosts/stats.getDeployableModules();
        simulatorHostsPerSimulatorModules = simulatorHosts/stats.getSimulatorModules();
        deployableCpu = Math.round(baseDcStats.deployableCpu + (baseDcStats.deployableCpuRate*addWeeks));
        deployableCpuRate = baseDcStats.deployableCpuRate;
        allHostsPerService = (deployableHosts+simulatorHosts)/stats.getServices();
        deployableCpusPerDeployableHosts = deployableCpu/deployableHosts;
        deployableCpusPerDeployableModules = deployableCpu/stats.getDeployableModules();
    }

    public double getDeployableHosts() {
        return deployableHosts;
    }

    public double getDeployableHostsRate() {
        return deployableHostsRate;
    }

    public double getSimulatorHosts() {
        return simulatorHosts;
    }

    public double getSimulatorHostsRate() {
        return simulatorHostsRate;
    }

    public double getAllHostsPerService() {
        return allHostsPerService;
    }

    public double getDeployableHostsPerDeployableModules() {
        return deployableHostsPerDeployableModules;
    }

    public double getSimulatorHostsPerSimulatorModules() {
        return simulatorHostsPerSimulatorModules;
    }

    public double getDeployableCpu() {
        return deployableCpu;
    }

    public double getDeployableCpuRate() {
        return deployableCpuRate;
    }

    public double getDeployableCpusPerDeployableHosts() {
        return deployableCpusPerDeployableHosts;
    }

    public double getDeployableCpusPerDeployableModules() {
        return deployableCpusPerDeployableModules;
    }

}
