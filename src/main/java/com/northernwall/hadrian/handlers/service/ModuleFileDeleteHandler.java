package com.northernwall.hadrian.handlers.service;

import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.GMT;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.handlers.utility.routingHandler.Http400BadRequestException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

import static com.northernwall.hadrian.handlers.BasicHandler.getGson;

/**
 * @author Thomas Chamberlain
 */
public class ModuleFileDeleteHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public ModuleFileDeleteHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        Service service = getService(request);
        Module module = getModule(request, service);
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "manage file for module");
        String network = request.getParameter("network");
        if (network == null || network.isEmpty()) {
            throw new Http400BadRequestException("parameter network is missing");
        }
        String fileName = getFileName(request);

        getDataAccess().deleteModuleFile(service.getServiceId(), module.getModuleId(), network, fileName);
        
        createAudit(
                service.getServiceId(),
                module.getModuleName(),
                user.getUsername(),
                "Delete file " + fileName + " on " + network);
        
        response.setStatus(200);
        request.setHandled(true);
    }

    private void createAudit(String serviceId, String moduleName, String requestor, String action) {
        Map<String, String> notes = new HashMap<>();
        notes.put("action", action);
        Audit audit = new Audit();
        audit.serviceId = serviceId;
        audit.moduleName = moduleName;
        audit.setTimePerformed(GMT.getGmtAsDate());
        audit.timeRequested = GMT.getGmtAsDate();
        audit.requestor = requestor;
        audit.type = Type.module;
        audit.operation = Operation.update;
        audit.successfull = true;
        audit.notes = getGson().toJson(notes);
        getDataAccess().saveAudit(audit, null);
    }

}
