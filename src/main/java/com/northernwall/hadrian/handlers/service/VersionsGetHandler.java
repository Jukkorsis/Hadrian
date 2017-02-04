package com.northernwall.hadrian.handlers.service;

import com.google.gson.Gson;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.module.ModuleArtifactHelper;
import com.northernwall.hadrian.module.ModuleConfigHelper;
import com.northernwall.hadrian.handlers.service.dao.GetVersionData;
import com.northernwall.hadrian.handlers.service.helper.ReadModuleArtifactVersionsRunnable;
import com.northernwall.hadrian.handlers.service.helper.ReadModuleConfigVersionsRunnable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

public class VersionsGetHandler extends BasicHandler {
    private final ModuleArtifactHelper moduleArtifactHelper;
    private final ModuleConfigHelper moduleConfigHelper;
    private final ExecutorService executorService;

    public VersionsGetHandler(DataAccess dataAccess, Gson gson, ModuleArtifactHelper moduleArtifactHelper, ModuleConfigHelper moduleConfigHelper) {
        super(dataAccess, gson);
        this.moduleArtifactHelper = moduleArtifactHelper;
        this.moduleConfigHelper = moduleConfigHelper;

        executorService = Executors.newFixedThreadPool(20);
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType(Const.JSON);
        Service service = getService(request);
        Module module = getModule(request, service);

        GetVersionData data = new GetVersionData();
        
        Future artifactFuture = executorService.submit(new ReadModuleArtifactVersionsRunnable(service, module, data, moduleArtifactHelper));
        Future configFuture = executorService.submit(new ReadModuleConfigVersionsRunnable(module, data, moduleConfigHelper));
        
        while (!artifactFuture.isDone() || !configFuture.isDone()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        }
        
        toJson(response, data);
        response.setStatus(200);
        request.setHandled(true);
    }

}
