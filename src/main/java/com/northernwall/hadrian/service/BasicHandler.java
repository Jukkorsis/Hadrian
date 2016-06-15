package com.northernwall.hadrian.service;

import com.google.gson.Gson;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http400BadRequestException;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http404NotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.eclipse.jetty.server.Request;
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

    public DataAccess getDataAccess() {
        return dataAccess;
    }

    public static Gson getGson() {
        return gson;
    }

    protected final <T> T fromJson(org.eclipse.jetty.server.Request request, Class<T> classOfT) throws IOException {
        Reader reader = new InputStreamReader(request.getInputStream());
        T temp = gson.fromJson(reader, classOfT);
        if (temp == null) {
            logger.warn("Stream->Json returned null");
            throw new Http400BadRequestException("JSON payload is missing");
        }
        logger.info("Stream->Json {}", gson.toJson(temp));
        return temp;
    }

    protected Team getTeam(Request request) {
        return getTeam(
                request.getParameter("teamId"),
                request.getParameter("teamName"));
    }

    protected Team getTeam(String teamId, String teamName) {
        if (teamId != null && !teamId.isEmpty()) {
            Team team = dataAccess.getTeam(teamId);
            if (team != null) {
                return team;
            }
            throw new Http404NotFoundException("Could not find team with ID " + teamId);
        }
        if (teamName != null && !teamName.isEmpty()) {
            for (Team team : dataAccess.getTeams()) {
                if (team.getTeamName().equalsIgnoreCase(teamName)) {
                    return team;
                }
            }
        }
        throw new Http404NotFoundException("Could not find team");
    }

    protected Service getService(Request request) {
        return getService(
                request.getParameter("serviceId"),
                request.getParameter("serviceName"),
                request.getParameter("serviceAbbr"));
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
            for (Service service : dataAccess.getActiveServices()) {
                if (service.getServiceName().equalsIgnoreCase(serviceName)) {
                    return service;
                }
            }
        }
        if (serviceAbbr != null && !serviceAbbr.isEmpty()) {
            for (Service service : dataAccess.getActiveServices()) {
                if (service.getServiceAbbr().equalsIgnoreCase(serviceAbbr)) {
                    return service;
                }
            }
        }
        throw new Http404NotFoundException("Could not find service");
    }

    protected Module getModule(Request request, Service service) {
        return getModule(
                request.getParameter("moduleId"),
                request.getParameter("moduleName"),
                service);
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

    protected Host getHost(Request request, Service service) {
        return getHost(
                request.getParameter("hostId"),
                request.getParameter("hostName"),
                service);
    }

    protected Host getHost(String hostId, String hostName, Service service) {
        if (hostId != null && !hostId.isEmpty()) {
            Host host = dataAccess.getHost(service.getServiceId(), hostId);
            if (host != null) {
                return host;
            }
        }
        if (hostName != null && !hostName.isEmpty()) {
            for (Host host : dataAccess.getHosts(service.getServiceId())) {
                if (host.getHostName().equalsIgnoreCase(hostName)) {
                    return host;
                }
            }
        }
        throw new Http404NotFoundException("Could not find host");
    }

    protected Vip getVip(Request request, Service service) {
        return getVip(
                request.getParameter("vipId"),
                service);
    }

    protected Vip getVip(String vipId, Service service) {
        if (vipId != null && !vipId.isEmpty()) {
            Vip vip = dataAccess.getVip(service.getServiceId(), vipId);
            if (vip != null) {
                return vip;
            }
        }
        throw new Http404NotFoundException("Could not find vip");
    }

}
