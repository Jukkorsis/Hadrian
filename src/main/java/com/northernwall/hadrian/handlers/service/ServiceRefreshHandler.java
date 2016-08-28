package com.northernwall.hadrian.handlers.service;

import com.northernwall.hadrian.handlers.BasicHandler;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.ConfigHelper;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleRef;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.handlers.service.dao.GetHostData;
import com.northernwall.hadrian.handlers.service.dao.GetModuleData;
import com.northernwall.hadrian.handlers.service.dao.GetModuleRefData;
import com.northernwall.hadrian.handlers.service.dao.GetServiceData;
import com.northernwall.hadrian.handlers.service.helper.InfoHelper;
import com.northernwall.hadrian.handlers.service.helper.ReadAvailabilityRunnable;
import com.northernwall.hadrian.handlers.service.helper.ReadVersionRunnable;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

import static com.northernwall.hadrian.handlers.BasicHandler.getGson;

public class ServiceRefreshHandler extends BasicHandler {

    private final AccessHelper accessHelper;
    private final ConfigHelper configHelper;
    private final InfoHelper infoHelper;
    private final ExecutorService executorService;

    public ServiceRefreshHandler(AccessHelper accessHelper, DataAccess dataAccess, ConfigHelper configHelper, InfoHelper infoHelper) {
        super(dataAccess);
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
        getServiceData.canModify = accessHelper.canUserModify(request, service.getTeamId());

        if (service.isActive()) {
            getModuleInfo(service, getServiceData, false);
            List<Future> futures = new LinkedList<>();
            getHostInfo(service, getServiceData, futures);
            waitForFutures(futures, 151, 100);
        }

        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            getGson().toJson(getServiceData, GetServiceData.class, jw);
        }
        response.setStatus(200);
        request.setHandled(true);
    }

    protected void getModuleInfo(Service service, GetServiceData getServiceData, boolean includeStuff) {
        List<Module> modules = getDataAccess().getModules(service.getServiceId());
        Collections.sort(modules);

        List<String> activeNetworks = new LinkedList<>();
        for (Module module : modules) {
            module.cleanNetworkNames(activeNetworks);
        }
        arrangeNetworks(getServiceData, activeNetworks, modules);
        for (Module module : modules) {
            GetModuleData getModuleData = GetModuleData.create(module, configHelper.getConfig());
            if (includeStuff) {
                getModuleRefInfo(module, getModuleData);
            }
            getServiceData.modules.add(getModuleData);
        }
    }

    private void arrangeNetworks(GetServiceData getServiceData, List<String> activeNetworks, List<Module> modules) {
        for (String network : configHelper.getConfig().networkNames) {
            if (activeNetworks.contains(network)) {
                getServiceData.addNetwork(network);
            }
        }
        for (String network : activeNetworks) {
            for (Module module : modules) {
                if (module.getNetworkNames() != null
                        && !module.getNetworkNames().isEmpty()
                        && module.getNetworkNames().containsKey(network)) {
                    getServiceData.addModuleNetwork(module, network);
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