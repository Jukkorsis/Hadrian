package com.northernwall.hadrian.service;

import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.ConfigHelper;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleRef;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.VipRef;
import com.northernwall.hadrian.maven.MavenHelper;
import com.northernwall.hadrian.service.dao.GetHostData;
import com.northernwall.hadrian.service.dao.GetModuleData;
import com.northernwall.hadrian.service.dao.GetModuleRefData;
import com.northernwall.hadrian.service.dao.GetServiceData;
import com.northernwall.hadrian.service.dao.GetVipData;
import com.northernwall.hadrian.service.dao.GetVipRefData;
import com.northernwall.hadrian.service.helper.InfoHelper;
import com.northernwall.hadrian.service.helper.ReadAvailabilityRunnable;
import com.northernwall.hadrian.service.helper.ReadMavenVersionsRunnable;
import com.northernwall.hadrian.service.helper.ReadVersionRunnable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

import static com.northernwall.hadrian.service.BasicHandler.getGson;

public class ServiceRefreshHandler extends BasicHandler {

    private final AccessHelper accessHelper;
    private final ConfigHelper configHelper;
    private final MavenHelper mavenHelper;
    private final InfoHelper infoHelper;
    private final ExecutorService executorService;

    public ServiceRefreshHandler(AccessHelper accessHelper, DataAccess dataAccess, ConfigHelper configHelper, MavenHelper mavenHelper, InfoHelper infoHelper) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.configHelper = configHelper;
        this.mavenHelper = mavenHelper;
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
            List<Future> futures = new LinkedList<>();

            getModuleInfo(service, getServiceData, false, futures);

            getHostInfo(service, getServiceData, futures);

            waitForFutures(futures);
        }

        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            getGson().toJson(getServiceData, GetServiceData.class, jw);
        }
        response.setStatus(200);
        request.setHandled(true);
    }

    protected void getModuleInfo(Service service, GetServiceData getServiceData, boolean includeStuff, List<Future> futures) {
        List<Module> modules = getDataAccess().getModules(service.getServiceId());
        Collections.sort(modules);
        for (Module module : modules) {
            GetModuleData getModuleData = GetModuleData.create(module, configHelper.getConfig());
            if (includeStuff) {
                futures.add(executorService.submit(new ReadMavenVersionsRunnable(getModuleData, mavenHelper)));
                getModuleRefInfo(module, getModuleData);
            }
            getServiceData.modules.add(getModuleData);
        }
    }

    private void getModuleRefInfo(Module module, GetModuleData getModuleData) {
        for (ModuleRef ref : getDataAccess().getModuleRefsByClient(module.getServiceId(), module.getModuleId())) {
            GetModuleRefData tempRef = GetModuleRefData.create(ref);
            Service serverService = getService(ref.getServerServiceId(), null, null);
            tempRef.serviceName = serverService.getServiceName();
            tempRef.moduleName = getModule(ref.getServerModuleId(), null, serverService).getModuleName();
            getModuleData.uses.add(tempRef);
        }
        
        Collections.sort(getModuleData.uses);
        
        for (ModuleRef ref : getDataAccess().getModuleRefsByServer(module.getServiceId(), module.getModuleId())) {
            GetModuleRefData tempRef = GetModuleRefData.create(ref);
            Service clientService = getService(ref.getClientServiceId(), null, null);
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
                futures.add(executorService.submit(new ReadVersionRunnable(getHostData, getModuleData, infoHelper)));
                futures.add(executorService.submit(new ReadAvailabilityRunnable(getHostData, getModuleData, infoHelper)));
                for (VipRef vipRef : getDataAccess().getVipRefsByHost(getHostData.hostId)) {
                    GetVipRefData getVipRefData = GetVipRefData.create(vipRef);
                    for (GetVipData vip : getModuleData.getVips(host.getNetwork())) {
                        if (vip.vipId.equals(getVipRefData.vipId)) {
                            getVipRefData.vipName = vip.vipName;
                        }
                    }
                    getHostData.vipRefs.add(getVipRefData);
                }
                getModuleData.addHost(getHostData);
            }
        }
    }

    protected void waitForFutures(List<Future> futures) {
        for (int i = 0; i < 20; i++) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
            }
            futures.removeIf(new Predicate<Future>() {
                @Override
                public boolean test(Future t) {
                    return t.isDone();
                }
            });
            if (futures.isEmpty()) {
                return;
            }
        }
    }

}
