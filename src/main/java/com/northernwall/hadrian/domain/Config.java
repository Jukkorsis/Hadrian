package com.northernwall.hadrian.domain;

import java.util.LinkedList;
import java.util.List;

public class Config {
    public String versionUrl = "";
    public String availabilityUrl = "";
    public String startCmd = "";
    public String stopCmd = "";
    public List<String> dataCenters = new LinkedList<>();
    public List<String> networks = new LinkedList<>();
    public List<String> envs = new LinkedList<>();
    public List<String> sizes = new LinkedList<>();
    public List<String> protocols = new LinkedList<>();
    public List<String> domains = new LinkedList<>();
    public List<String> artifactTypes = new LinkedList<>();
    public List<String> templates = new LinkedList<>();
    public List<String> businessImpacts = new LinkedList<>();
    public List<String> piiUsages = new LinkedList<>();
    
}
