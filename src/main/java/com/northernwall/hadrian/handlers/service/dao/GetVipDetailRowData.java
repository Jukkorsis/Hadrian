package com.northernwall.hadrian.handlers.service.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GetVipDetailRowData {
    public String hostName;
    public Map<String, GetVipDetailCellData> details = new ConcurrentHashMap<>();
    public String warning = "-";

}