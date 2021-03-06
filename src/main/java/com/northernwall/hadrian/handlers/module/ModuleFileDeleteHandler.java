package com.northernwall.hadrian.handlers.module;

import com.google.gson.Gson;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.GMT;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.handlers.routing.Http400BadRequestException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 * @author Thomas Chamberlain
 */
public class ModuleFileDeleteHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public ModuleFileDeleteHandler(DataAccess dataAccess, Gson gson, AccessHelper accessHelper) {
        super(dataAccess, gson);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        Service service = getService(request);
        Team team = getTeam(service.getTeamId(), null);
        Module module = getModule(request, service);
        User user = accessHelper.checkIfUserCanModify(request, team, "manage file for module");
        String environment = request.getParameter("environment");
        if (environment == null || environment.isEmpty()) {
            throw new Http400BadRequestException("parameter environment is missing");
        }
        String fileName = getFileName(request);

        getDataAccess().deleteModuleFile(service.getServiceId(), module.getModuleId(), environment, fileName);
        
        createAudit(
                service.getServiceId(),
                module.getModuleName(),
                user.getUsername(),
                "Delete file " + fileName + " on " + environment);
        
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
