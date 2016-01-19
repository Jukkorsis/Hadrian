package com.northernwall.hadrian.workItem.dao;

public class CallbackData {

    /**
     * The value of the X-Request-Id header used to trigger the deployment.
     */
    public String requestId;

    /**
     * one of SUCCESS/FAILURE
     */
    public String status;

    /**
     * An optional code, provided by the service to assist in diagnosis.
     */
    public int errorCode;

    /**
     * An optional description provide by the service to assist in diagnosis.
     */
    public String errorDescription;

    /**
     * The console output (stdout and stderr) provided by the deployment.
     */
    public String output;
}
