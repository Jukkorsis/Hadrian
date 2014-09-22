package com.northernwall.hadrian.domain;

import java.util.LinkedList;
import java.util.List;

public class Env {
    public String name;
    public String vip;
    public List<Host> hosts = new LinkedList<>();
    
    public Host findHost(String name) {
        if (hosts == null || hosts.isEmpty()) {
            return null;
        }
        for (Host host : hosts) {
            if (host.name.equals(name)) {
                return host;
            }
        }
        return null;
    }

}
