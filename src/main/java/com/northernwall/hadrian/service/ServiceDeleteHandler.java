package com.northernwall.hadrian.service;

import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.DataStore;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.service.dao.DeleteServiceData;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http400BadRequestException;
import java.io.IOException;
import java.util.List;
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
        DeleteServiceData deleteServiceData = fromJson(request, DeleteServiceData.class);
        Service service = getService(deleteServiceData.serviceId, null, null);
        User user = accessHelper.checkIfUserCanModify(request, service.getTeamId(), "delete service");
        Team team = getDataAccess().getTeam(service.getTeamId());

        List<Module> modules = getDataAccess().getModules(deleteServiceData.serviceId);
        if (modules != null && !modules.isEmpty()) {
            throw new Http400BadRequestException("Can not delete service with an active module");
        }

        List<DataStore> dataStores = getDataAccess().getDataStores(deleteServiceData.serviceId);
        if (dataStores != null && !dataStores.isEmpty()) {
            throw new Http400BadRequestException("Can not delete service with an active data store");
        }

        //TODO delete from DB
    }

}
