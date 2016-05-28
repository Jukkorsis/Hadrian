package com.northernwall.hadrian.service;

import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.module.ModuleArtifactHelper;
import com.northernwall.hadrian.module.ModuleConfigHelper;
import com.northernwall.hadrian.service.dao.GetVersionData;
import com.northernwall.hadrian.service.helper.ReadModuleArtifactVersionsRunnable;
import com.northernwall.hadrian.service.helper.ReadModuleConfigVersionsRunnable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

import static com.northernwall.hadrian.service.BasicHandler.getGson;

public class VersionsGetHandler extends BasicHandler {
    private final ModuleArtifactHelper moduleArtifactHelper;
    private final ModuleConfigHelper moduleConfigHelper;
    private final ExecutorService executorService;

    public VersionsGetHandler(DataAccess dataAccess, ModuleArtifactHelper moduleArtifactHelper, ModuleConfigHelper moduleConfigHelper) {
        super(dataAccess);
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
        
        Future artifactFuture = executorService.submit(new ReadModuleArtifactVersionsRunnable(module, data, moduleArtifactHelper));
        Future configFuture = executorService.submit(new ReadModuleConfigVersionsRunnable(module, data, moduleConfigHelper));
        
        while (!artifactFuture.isDone() || !configFuture.isDone()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        }
        
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            getGson().toJson(data, GetVersionData.class, jw);
        }
        response.setStatus(200);
        request.setHandled(true);
    }

}
