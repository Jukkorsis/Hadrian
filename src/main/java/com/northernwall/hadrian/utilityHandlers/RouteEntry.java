package com.northernwall.hadrian.utilityHandlers;

import org.eclipse.jetty.server.Handler;

public class RouteEntry {

    public final String method;
    public final RouteType type;
    public final String target;
    public final Handler handler;
    public final String name;

    public RouteEntry(String method, RouteType type, String target, Handler handler) {
        this.method = method;
        this.type = type;
        this.target = target;
        this.handler = handler;
        this.name = handler.getClass().getSimpleName();
    }

}
