package com.northernwall.hadrian.handlers.service;

import com.google.gson.Gson;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.config.ConfigHelper;
import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.InboundProtocol;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleRef;
import com.northernwall.hadrian.domain.OutboundProtocol;
import com.northernwall.hadrian.domain.ProtocolModifier;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.handlers.host.dao.GetHostData;
import com.northernwall.hadrian.handlers.module.dao.GetModuleData;
import com.northernwall.hadrian.handlers.service.dao.GetModuleRefData;
import com.northernwall.hadrian.handlers.service.dao.GetServiceData;
import com.northernwall.hadrian.handlers.service.helper.InfoHelper;
import com.northernwall.hadrian.handlers.service.helper.ReadAvailabilityRunnable;
import com.northernwall.hadrian.handlers.service.helper.ReadVersionRunnable;
import com.northernwall.hadrian.handlers.vip.dao.GetVipData;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

public class ServiceRefreshHandler extends BasicHandler {

    private final AccessHelper accessHelper;
    protected final ConfigHelper configHelper;
    private final InfoHelper infoHelper;
    private final ExecutorService executorService;

    public ServiceRefreshHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper, ConfigHelper configHelper, InfoHelper infoHelper) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
        this.configHelper = configHelper;
        this.infoHelper = infoHelper;

        executorService = Executors.newFixedThreadPool(20);
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType(Const.JSON);
        Service service = getService(request);

        GetServiceData getServiceData = GetServiceData.create(service);
        Team team = getTeam(service.getTeamId(), null);
        getServiceData.canModify = accessHelper.canUserModify(request, team);

        if (service.isActive()) {
            getModuleInfo(service, getServiceData, false);
            getVipInfo(service, getServiceData);
            List<Future> futures = new LinkedList<>();
            getHostInfo(service, getServiceData, futures);
            waitForFutures(futures, 151, 100);
        }

        toJson(response, getServiceData);
        response.setStatus(200);
        request.setHandled(true);
    }

    protected void getModuleInfo(Service service, GetServiceData getServiceData, boolean includeStuff) {
        List<Module> modules = getDataAccess().getModules(service.getServiceId());
        Collections.sort(modules);

        List<String> activeEnvironments = new LinkedList<>();
        for (Module module : modules) {
            module.cleanEnvironmentNames(activeEnvironments);
        }
        arrangeEnvironments(getServiceData, activeEnvironments, modules);
        for (Module module : modules) {
            GetModuleData getModuleData = GetModuleData.create(module, configHelper.getConfig());
            if (includeStuff) {
                getModuleRefInfo(module, getModuleData);
            }
            getServiceData.modules.add(getModuleData);
        }
    }

    private void arrangeEnvironments(GetServiceData getServiceData, List<String> activeEnvironments, List<Module> modules) {
        for (String environment : configHelper.getConfig().environmentNames) {
            if (activeEnvironments.contains(environment)) {
                getServiceData.addEnvironment(environment);
            }
        }
        for (String environment : activeEnvironments) {
            for (Module module : modules) {
                if (module.getEnvironmentNames() != null
                        && !module.getEnvironmentNames().isEmpty()
                        && module.getEnvironmentNames().containsKey(environment)) {
                    getServiceData.addModuleEnvironment(module, environment);
                }
            }
        }
    }

    private void getModuleRefInfo(Module module, GetModuleData getModuleData) {
        for (ModuleRef ref : getDataAccess().getModuleRefsByClient(module.getServiceId(), module.getModuleId())) {
            GetModuleRefData tempRef = GetModuleRefData.create(ref);
            Service serverService = getService(ref.getServerServiceId(), null);
            tempRef.serviceName = serverService.getServiceName();
            tempRef.moduleName = getModule(ref.getServerModuleId(), null, serverService).getModuleName();
            getModuleData.uses.add(tempRef);
        }

        Collections.sort(getModuleData.uses);

        for (ModuleRef ref : getDataAccess().getModuleRefsByServer(module.getServiceId(), module.getModuleId())) {
            GetModuleRefData tempRef = GetModuleRefData.create(ref);
            Service clientService = getService(ref.getClientServiceId(), null);
            tempRef.serviceName = clientService.getServiceName();
            tempRef.moduleName = getModule(ref.getClientModuleId(), null, clientService).getModuleName();
            getModuleData.usedBy.add(tempRef);
        }

        Collections.sort(getModuleData.usedBy);
    }

    protected void getVipInfo(Service service, GetServiceData getServiceData) {
        List<Vip> vips = getDataAccess().getVips(service.getServiceId());
        Collections.sort(vips);
        List<InboundProtocol> inboundProtocols = configHelper.getConfig().inboundProtocols;
        for (Vip vip : vips) {
            GetModuleData getModuleData = null;
            for (GetModuleData temp : getServiceData.modules) {
                if (vip.getModuleId().equals(temp.moduleId)) {
                    getModuleData = temp;
                }
            }
            if (getModuleData != null) {
                GetVipData getVipData = GetVipData.create(vip);
                generateVipText(getVipData, vip, inboundProtocols);
                getServiceData.addVip(getVipData, getModuleData);
            }
        }
    }

    private void generateVipText(GetVipData getVipData, Vip vip, List<InboundProtocol> inboundProtocols) {
        InboundProtocol inboundProtocol = null;
        for (InboundProtocol temp : inboundProtocols) {
            if (temp.code.equals(vip.getInboundProtocol())) {
                inboundProtocol = temp;
            }
        }
        
        if (inboundProtocol == null) {
            getVipData.inboundText = vip.getInboundProtocol();
            getVipData.outboundText = vip.getOutboundProtocol();
            return;
        }
        
        if (inboundProtocol.vipPortRequired) {
            getVipData.inboundText = inboundProtocol.name + " (" + vip.getVipPort() + ")";
        } else {
            getVipData.inboundText = inboundProtocol.name;
        }
        if (vip.getInboundModifiers() != null && !vip.getInboundModifiers().isEmpty()) {
            getVipData.inboundText = getVipData.inboundText + " [";
            boolean first = true;
            for (String modifier : vip.getInboundModifiers()) {
                if (first) {
                    first = false;
                    getVipData.inboundText = getVipData.inboundText
                            + generateModiferText(vip, modifier, inboundProtocol.modifiers);
                } else {
                    getVipData.inboundText = getVipData.inboundText
                            + ", "
                            + generateModiferText(vip, modifier, inboundProtocol.modifiers);
                }
            }
            getVipData.inboundText = getVipData.inboundText + "]";
        }

        OutboundProtocol outboundProtocol = null;
        for (OutboundProtocol temp : inboundProtocol.outbound) {
            if (temp.code.equals(vip.getOutboundProtocol())) {
                outboundProtocol = temp;
            }
        }
        if (outboundProtocol == null) {
            getVipData.outboundText = vip.getOutboundProtocol() + " (" + vip.getServicePort() + ")";
        } else {
            getVipData.outboundText = outboundProtocol.name + " (" + vip.getServicePort() + ")";
        }
        if (vip.getOutboundModifiers() != null && !vip.getOutboundModifiers().isEmpty()) {
            getVipData.outboundText = getVipData.outboundText + " [";
            boolean first = true;
            for (String modifier : vip.getOutboundModifiers()) {
                if (first) {
                    first = false;
                    getVipData.outboundText = getVipData.outboundText
                            + generateModiferText(vip, modifier, outboundProtocol.modifiers);
                } else {
                    getVipData.outboundText = getVipData.outboundText
                            + ", "
                            + generateModiferText(vip, modifier, outboundProtocol.modifiers);
                }
            }
            getVipData.outboundText = getVipData.outboundText + "]";
        }
    }

    private String generateModiferText(Vip vip, String modifier, List<ProtocolModifier> protocolModifiers) {
        if (protocolModifiers != null && !protocolModifiers.isEmpty()) {
            for (ProtocolModifier protocolModifier : protocolModifiers) {
                if (protocolModifier.code.equals(modifier)) {
                    if (protocolModifier.httpCheckPortRequired) {
                        return protocolModifier.name
                                + " ("
                                + vip.getHttpCheckPort()
                                + ")";
                    } else {
                        return protocolModifier.name;
                    }
                }
            }
            return modifier;
        }
        return "";
    }

    protected void getHostInfo(Service service, GetServiceData getServiceData, List<Future> futures) {
        List<Host> hosts = getDataAccess().getHosts(service.getServiceId());
        Collections.sort(hosts);
        for (Host host : hosts) {
            GetModuleData getModuleData = null;
            for (GetModuleData temp : getServiceData.modules) {
                if (host.getModuleId().equals(temp.moduleId)) {
                    getModuleData = temp;
                }
            }
            if (getModuleData != null) {
                GetHostData getHostData = GetHostData.create(host);
                getServiceData.addHost(getHostData, getModuleData);
                futures.add(executorService.submit(new ReadVersionRunnable(getHostData, getModuleData, infoHelper)));
                futures.add(executorService.submit(new ReadAvailabilityRunnable(getHostData, getModuleData, infoHelper)));
            }
        }
    }

}
