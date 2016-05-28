package com.northernwall.hadrian.service;

import com.northernwall.hadrian.GMT;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.DataStore;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.service.dao.DeleteServiceData;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http400BadRequestException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

public class ServiceDeleteHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public ServiceDeleteHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        DeleteServiceData data = fromJson(request, DeleteServiceData.class);
        Service service = getService(data.serviceId, null, null);
        getTeam(service.getTeamId(), null);
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "delete service");

        List<Module> modules = getDataAccess().getModules(data.serviceId);
        if (modules != null && !modules.isEmpty()) {
            throw new Http400BadRequestException("Can not delete a service with an active module");
        }

        List<DataStore> dataStores = getDataAccess().getDataStores(data.serviceId);
        if (dataStores != null && !dataStores.isEmpty()) {
            throw new Http400BadRequestException("Can not delete a service with an active data store");
        }

        service.setActive(false);
        service.setDeletionDate(GMT.getGmtAsDate());
        getDataAccess().saveService(service);

        Audit audit = new Audit();
        audit.serviceId = service.getServiceId();
        audit.timePerformed = GMT.getGmtAsDate();
        audit.timeRequested = GMT.getGmtAsDate();
        audit.requestor = user.getUsername();
        audit.type = Type.service;
        audit.operation = Operation.delete;
        audit.successfull = true;
        Map<String, String> notes = new HashMap<>();
        notes.put("reason", data.reason);
        audit.notes = getGson().toJson(notes);
        getDataAccess().saveAudit(audit, null);
        
        response.setStatus(200);
        request.setHandled(true);
    }

}
