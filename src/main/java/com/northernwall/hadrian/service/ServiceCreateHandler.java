/*
 * Copyright 2014 Richard Thurston.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.northernwall.hadrian.service;

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.GitMode;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Operation;
import com.northernwall.hadrian.domain.Type;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.service.dao.PostServiceData;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class ServiceCreateHandler extends BasicHandler {

    private final static Logger logger = LoggerFactory.getLogger(ServiceCreateHandler.class);

    private final AccessHelper accessHelper;

    public ServiceCreateHandler(AccessHelper accessHelper, DataAccess dataAccess) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostServiceData postServiceData = fromJson(request, PostServiceData.class);
        User user = accessHelper.checkIfUserCanModify(request, postServiceData.teamId, "create a service");
        postServiceData.serviceAbbr = postServiceData.serviceAbbr.toUpperCase();
        if (!postServiceData.serviceAbbr.matches("\\w+")) {
            logger.warn("Illegal service Abbr");
            return;
        }

        for (Service temp : getDataAccess().getServices(postServiceData.teamId)) {
            if (temp.getServiceAbbr().equals(postServiceData.serviceAbbr)) {
                logger.warn("A service already exists with that abbreviation, {}", postServiceData.serviceAbbr);
                return;
            }
        }

        if (postServiceData.serviceType.equals(Const.SERVICE_TYPE_SHARED_LIBRARY)) {
            postServiceData.gitMode = GitMode.Flat;
        }

        Service service = new Service(
                postServiceData.serviceAbbr,
                postServiceData.serviceName,
                postServiceData.teamId,
                postServiceData.description,
                postServiceData.serviceType,
                postServiceData.gitMode,
                postServiceData.gitProject);

        getDataAccess().saveService(service);

        Map<String, String> notes = new HashMap<>();
        notes.put("name", service.getServiceName());
        notes.put("abbr", service.getServiceAbbr());
        createAudit(service.getServiceId(), user.getUsername(), Type.service, Operation.create, notes);
        response.setStatus(200);
        request.setHandled(true);
    }

    private void createAudit(String serviceId, String requestor, Type type, Operation operation, Map<String, String> notes) {
        Audit audit = new Audit();
        audit.serviceId = serviceId;
        audit.timePerformed = new Date();
        audit.timeRequested = new Date();
        audit.requestor = requestor;
        audit.type = type;
        audit.operation = operation;
        audit.notes = getGson().toJson(notes);
        getDataAccess().saveAudit(audit, " ");
    }

}
