package com.northernwall.hadrian.handlers.service;

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.handlers.BasicHandler;
import com.northernwall.hadrian.GMT;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.DataStore;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.handlers.service.dao.DeleteServiceData;
import com.northernwall.hadrian.handlers.utility.routingHandler.Http400BadRequestException;
import com.northernwall.hadrian.workItem.WorkItemProcessor;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

public class ServiceDeleteHandler extends BasicHandler {

    private final AccessHelper accessHelper;
    private final WorkItemProcessor workItemProcessor;

    public ServiceDeleteHandler(AccessHelper accessHelper, DataAccess dataAccess, WorkItemProcessor workItemProcessor) {
        super(dataAccess);
        this.accessHelper = accessHelper;
        this.workItemProcessor = workItemProcessor;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        DeleteServiceData data = fromJson(request, DeleteServiceData.class);
        Service service = getService(data.serviceId, null);
        Team team = getTeam(service.getTeamId(), null);
        User user = accessHelper.checkIfUserCanModify(request, team, "delete service");

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
        getDataAccess().deleteSearch(
                Const.SEARCH_SPACE_SERVICE_NAME,
                service.getServiceName());
        getDataAccess().deleteSearch(
                Const.SEARCH_SPACE_GIT_PROJECT,
                service.getGitProject());

        WorkItem workItem = new WorkItem(Type.service, Operation.delete, user, team, service, null, null, null, data.reason);
        workItemProcessor.processWorkItem(workItem);
        
        response.setStatus(200);
        request.setHandled(true);
    }

}
