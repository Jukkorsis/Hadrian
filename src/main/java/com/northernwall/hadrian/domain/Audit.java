package com.northernwall.hadrian.domain;

import java.util.Date;

public class Audit {
    public String serviceId;
    public Date timePerformed;
    public Date timeRequested;
    public String requestor;
    public String type;
    public String operation;
    public String hostName;
    public String vipName;
    public String notes;

}
