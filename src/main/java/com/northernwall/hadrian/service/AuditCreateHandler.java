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

import com.northernwall.hadrian.GMT;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.service.dao.PostAuditData;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

public class AuditCreateHandler extends BasicHandler {

    private final AccessHelper accessHelper;

    public AuditCreateHandler(DataAccess dataAccess, AccessHelper accessHelper) {
        super(dataAccess);
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostAuditData data = fromJson(request, PostAuditData.class);

        Service service = getService(data.serviceId, data.serviceName, data.serviceAbbr);
        User user = accessHelper.checkIfUserCanAudit(request, service.getTeamId());

        Audit audit = new Audit();
        audit.serviceId = service.getServiceId();
        audit.timePerformed = GMT.getGmtAsDate();
        audit.timeRequested = GMT.getGmtAsDate();
        audit.requestor = user.getUsername();
        audit.type = data.type;
        audit.operation = data.operation;
        if (data.hostName != null) {
            audit.hostName = data.hostName;
        }
        if (data.vipName != null) {
            audit.vipName = data.vipName;
        }
        audit.notes = data.notes;
        getDataAccess().saveAudit(audit, data.output);

        response.setStatus(200);
        request.setHandled(true);
    }

}
