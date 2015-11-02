package com.northernwall.hadrian.service.dao;

import com.northernwall.hadrian.domain.CustomFunction;

public class GetCustomFunctionData {
    public String customFunctionId;
    public String name;
    public String serviceId;
    public String method;
    public String url;
    public String helpText;
    public boolean teamOnly;

    public static GetCustomFunctionData create(CustomFunction customFunction) {
        GetCustomFunctionData temp = new GetCustomFunctionData();
        temp.customFunctionId = customFunction.getCustomFunctionId();
        temp.name = customFunction.getName();
        temp.serviceId = customFunction.getServiceId();
        temp.method = customFunction.getMethod();
        temp.url = customFunction.getUrl();
        temp.helpText = customFunction.getHelpText();
        temp.teamOnly = customFunction.isTeamOnly();
        return temp;
    }

}