package com.northernwall.hadrian.service;

import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http404NotFoundException;
import org.eclipse.jetty.server.handler.AbstractHandler;

public abstract class BasicHandler extends AbstractHandler {

    private final DataAccess dataAccess;

    public BasicHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    protected Service getService(String serviceId, String serviceName) {
        if (serviceId != null && !serviceId.isEmpty()) {
            Service service = dataAccess.getService(serviceId);
            if (service != null) {
                return service;
            }
        }
        if (serviceName != null && !serviceName.isEmpty()) {
            for (Service service : dataAccess.getServices()) {
                if (service.getServiceName().equalsIgnoreCase(serviceName)) {
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

}
