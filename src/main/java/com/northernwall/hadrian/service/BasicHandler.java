package com.northernwall.hadrian.service;

import com.google.gson.Gson;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http404NotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasicHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(BasicHandler.class);
    private final static Gson gson = new Gson();
    
    private final DataAccess dataAccess;

    public BasicHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    protected final <T> T fromJson(org.eclipse.jetty.server.Request request, Class<T> classOfT) throws IOException {
        Reader reader = new InputStreamReader(request.getInputStream());
        T temp = gson.fromJson(reader, classOfT);
        logger.info("Stream->Json {}", gson.toJson(temp));
        return temp;
    }

    protected Service getService(String serviceId, String serviceName, String serviceAbbr) {
        if (serviceId != null && !serviceId.isEmpty()) {
            Service service = dataAccess.getService(serviceId);
            if (service != null) {
                return service;
            }
            throw new Http404NotFoundException("Could not find service with ID " + serviceId);
        }
        if (serviceName != null && !serviceName.isEmpty()) {
            for (Service service : dataAccess.getServices()) {
                if (service.getServiceName().equalsIgnoreCase(serviceName)) {
                    return service;
                }
            }
        }
        if (serviceAbbr != null && !serviceAbbr.isEmpty()) {
            for (Service service : dataAccess.getServices()) {
                if (service.getServiceAbbr().equalsIgnoreCase(serviceAbbr)) {
                    return service;
                }
            }
        }
        throw new Http404NotFoundException("Could not find service");
    }

    protected Module getModule(String moduleId, String moduleName, Service service) {
        if (moduleId != null && !moduleId.isEmpty()) {
            Module module = dataAccess.getModule(service.getServiceId(), moduleId);
            if (module != null) {
                return module;
            }
        }
        if (moduleName != null && !moduleName.isEmpty()) {
            for (Module module : dataAccess.getModules(service.getServiceId())) {
                if (module.getModuleName().equalsIgnoreCase(moduleName)) {
                    return module;
                }
            }
        }
        throw new Http404NotFoundException("Could not find module");
    }

    protected Date getGmt() {
        return Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
    }
    
}
