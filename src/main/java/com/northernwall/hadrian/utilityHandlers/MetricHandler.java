package com.northernwall.hadrian.utilityHandlers;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.northernwall.hadrian.Const;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(AvailabilityHandler.class);

    private final HandlerList handlers;
    private final Timer timerGet;
    private final Timer timerPost;
    private final Timer timerPut;
    private final Timer timerDelete;
    private final Meter meter200;
    private final Meter meter400;
    private final Meter meter500;

    public MetricHandler(HandlerList handlers, MetricRegistry metricRegistry) {
        this.handlers = handlers;
        timerGet = metricRegistry.timer("http.get");
        timerPost = metricRegistry.timer("http.post");
        timerPut = metricRegistry.timer("http.put");
        timerDelete = metricRegistry.timer("http.delete");
        meter200 = metricRegistry.meter("http.200");
        meter400 = metricRegistry.meter("http.400");
        meter500 = metricRegistry.meter("http.500");
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        Context context = null;
        switch (request.getMethod()) {
            case Const.HTTP_GET:
                context = timerGet.time();
                break;
            case Const.HTTP_POST:
                context = timerPost.time();
                break;
            case Const.HTTP_PUT:
                context = timerPut.time();
                break;
            case Const.HTTP_DELETE:
                context = timerDelete.time();
                break;
        }
        try {
            handlers.handle(target, request, httpRequest, response);
            
            if (response.getStatus() < 400) {
                meter200.mark();
            } else if (response.getStatus() < 500) {
                meter400.mark();
            } else {
                meter500.mark();
            }
        } finally {
            if (context != null) {
                context.stop();
            }
        }
    }

}
