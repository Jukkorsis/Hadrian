package com.northernwall.hadrian.service.dao;

import com.northernwall.hadrian.domain.CustomFunction;

public class GetCustomFunctionData {
    public String customFunctionId;
    public String name;
    public String serviceId;
    public String protocol;
    public String url;
    public String style;
    public String helpText;

    public static GetCustomFunctionData create(CustomFunction customFunction) {
        GetCustomFunctionData temp = new GetCustomFunctionData();
        temp.customFunctionId = customFunction.getCustomFunctionId();
        temp.name = customFunction.getName();
        temp.serviceId = customFunction.getServiceId();
        temp.protocol = customFunction.getProtocol();
        temp.url = customFunction.getUrl();
        temp.style = customFunction.getStyle();
        temp.helpText = customFunction.getHelpText();
        return temp;
    }

}
