package com.northernwall.hadrian.domain;

import java.util.Date;

public class Audit implements Comparable<Audit> {
    public String serviceId;
    public Date timePerformed;
    public Date timeRequested;
    public String requestor;
    public String type;
    public String operation;
    public String moduleName;
    public String hostName;
    public String vipName;
    public String notes;

    @Override
    public int compareTo(Audit o) {
        int result = timePerformed.compareTo(o.timePerformed);
        if (result != 0) {
            return result;
        }
        result = timeRequested.compareTo(o.timeRequested);
        if (result != 0) {
            return result;
        }
        return serviceId.compareTo(o.serviceId);
    }

}
