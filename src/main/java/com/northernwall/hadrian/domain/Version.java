package com.northernwall.hadrian.domain;

import java.util.List;

public class Version {
    public String api;
    public String impl;
    public String status;
    public List<Link> links;
    public List<ServiceRef> uses;
    public List<ServiceRef> usedby;

}
