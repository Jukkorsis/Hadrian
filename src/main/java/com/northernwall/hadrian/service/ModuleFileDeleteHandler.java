package com.northernwall.hadrian.service;

import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http400BadRequestException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 * @author Thomas Chamberlain
 */
public class ModuleFileDeleteHandler extends BasicHandler{

    private final AccessHelper accessHelper;

    public ModuleFileDeleteHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        Service service = getService(request);
        Module module = getModule(request, service);
        accessHelper.checkIfUserCanModify(request, service.getTeamId(), "manage file for module");
        String network = request.getParameter("network");
        if (network == null || network.isEmpty()) {
            throw new Http400BadRequestException("parameter network is missing");
        }
        String fileName = getFileName(request);

        getDataAccess().deleteModuleFile(service.getServiceId(), module.getModuleId(), network, fileName);

        response.setStatus(200);
        request.setHandled(true);
    }
}
