package com.northernwall.hadrian.domain;

import java.util.Date;

public class Audit {
    public String serviceId;
    public Date time;
    public String requestor;
    public String type;
    public String operation;
    public String hostname;
    public String vipname;
    public String notes;

}
