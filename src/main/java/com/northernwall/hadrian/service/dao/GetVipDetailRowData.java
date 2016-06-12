package com.northernwall.hadrian.service.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GetVipDetailRowData {
    public String hostName;
    public Map<String, GetVipDetailCellData> details = new ConcurrentHashMap<>();

}
