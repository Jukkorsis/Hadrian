package com.northernwall.hadrian.utilityHandlers.routingHandler;

import org.eclipse.jetty.server.Handler;

public class RouteEntry {

    public final MethodRule methodRule;
    public final TargetRule targetRule;
    public final String targetPattern;
    public final Handler handler;
    public final String name;

    public RouteEntry(MethodRule methodRule, TargetRule targetRule, String targetPattern, Handler handler) {
        this.methodRule = methodRule;
        this.targetRule = targetRule;
        this.targetPattern = targetPattern;
        this.handler = handler;
        this.name = handler.getClass().getSimpleName();
    }

}
