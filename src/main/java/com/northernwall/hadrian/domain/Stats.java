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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Richard Thurston
 */
public class Stats {
    private int year;
    private int week;
    private double services;
    private double deployableModules;
    private double libraryModules;
    private double simulatorModules;
    private double testModules;
    private double allModulesPerService;
    private Map<String, DcStats> dcStats;

    public Stats(int year, int week, double services, double deployableModules, double libraryModules, double simulatorModules, double testModules) {
        this.year = year;
        this.week = week;
        while (this.week > 52) {
            this.year++;
            this.week = this.week - 52;
        }
        
        this.services = services;
        this.deployableModules = deployableModules;
        this.libraryModules = libraryModules;
        this.simulatorModules = simulatorModules;
        this.testModules = testModules;
        
        if (services == 0) {
            allModulesPerService = 0;
        } else {
            allModulesPerService = (deployableModules+libraryModules+simulatorModules+testModules)/services;
        }

        dcStats = new HashMap<>();
    }
    
    public Stats(Stats baseStats, int addWeeks) {
        this.year = baseStats.year;
        this.week = baseStats.week + addWeeks;
        while (this.week > 52) {
            this.year++;
            this.week = this.week - 52;
        }
        
        this.services = baseStats.services;
        this.deployableModules = baseStats.deployableModules;
        this.libraryModules = baseStats.libraryModules;
        this.simulatorModules = baseStats.simulatorModules;
        this.testModules = baseStats.testModules;
        
        if (services == 0) {
            allModulesPerService = 0;
        } else {
            allModulesPerService = (deployableModules+libraryModules+simulatorModules+testModules)/services;
        }

        dcStats = new HashMap<>();
        for (String dc : baseStats.dcStats.keySet()) {
            DcStats temp = baseStats.dcStats.get(dc);
            dcStats.put(dc, new DcStats(baseStats, temp, addWeeks));
        }
    }

    public int getYear() {
        return year;
    }

    public int getWeek() {
        return week;
    }

    public double getServices() {
        return services;
    }

    public double getDeployableModules() {
        return deployableModules;
    }

    public double getLibraryModules() {
        return libraryModules;
    }

    public double getSimulatorModules() {
        return simulatorModules;
    }

    public double getTestModules() {
        return testModules;
    }
    
    public double getAllModulesPerService() {
        return allModulesPerService;
    }
    
    public void addDcStat(String dc, DcStats stats) {
        dcStats.put(dc, stats);
    }
    
    public DcStats getDcStats(String dc) {
        return dcStats.get(dc);
    }

}
