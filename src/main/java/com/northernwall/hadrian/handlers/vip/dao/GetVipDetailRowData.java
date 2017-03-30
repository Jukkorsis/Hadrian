package com.northernwall.hadrian.handlers.vip.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GetVipDetailRowData {
    
    /**
     * Name of the host in the pool
     */
    public String hostName;

    /**
     * Map of DC to cell level details
     */
    public Map<String, GetVipDetailCellData> details = new ConcurrentHashMap<>();
    
    /**
     * Short text describing any issues with this host in the pool
     */
    public String warning = "-";

}
