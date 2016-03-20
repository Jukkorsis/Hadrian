package com.northernwall.hadrian.utilityHandlers.routingHandler;

import org.eclipse.jetty.server.Handler;

public class RouteEntry {

    public final TargetRule targetRule;
    public final String targetPattern;
    public final Handler handler;
    public final String name;
    public final boolean logAccess;

    public RouteEntry(TargetRule targetRule, String targetPattern, Handler handler, boolean logAccess) {
        this.targetRule = targetRule;
        this.targetPattern = targetPattern;
        this.handler = handler;
        this.name = handler.getClass().getSimpleName();
        this.logAccess = logAccess;
    }

}
