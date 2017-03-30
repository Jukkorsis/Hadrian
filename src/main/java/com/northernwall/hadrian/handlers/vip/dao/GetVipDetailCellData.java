package com.northernwall.hadrian.handlers.vip.dao;

public class GetVipDetailCellData {

    /**
     * The host's priority within the pool for a specific DC's LB
     */
    public int priority;

    /**
     * The host's status within the pool for a specific DC's LB
     */
    public String status;

    /**
     * The number of connections from a specific DC's LB to the host
     */
    public int connections;
}
