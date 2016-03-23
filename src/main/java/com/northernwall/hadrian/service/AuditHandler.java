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

import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.service.dao.PostAudit;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

public class AuditHandler extends BasicHandler {

    private final DataAccess dataAccess;
    private final AccessHelper accessHelper;

    public AuditHandler(DataAccess dataAccess, AccessHelper accessHelper) {
        super(dataAccess);
        this.dataAccess = dataAccess;
        this.accessHelper = accessHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostAudit postAudit = fromJson(request, PostAudit.class);

        Service service = getService(postAudit.serviceId, postAudit.serviceName, postAudit.serviceAbbr);
        User user = accessHelper.checkIfUserCanAudit(request, service.getTeamId());

        Audit audit = new Audit();
        audit.serviceId = service.getServiceId();
        audit.timePerformed = getGmt();
        audit.timeRequested = getGmt();
        audit.requestor = user.getUsername();
        audit.type = postAudit.type;
        audit.operation = postAudit.operation;
        if (postAudit.hostName != null) {
            audit.hostName = postAudit.hostName;
        }
        if (postAudit.vipName != null) {
            audit.vipName = postAudit.vipName;
        }
        audit.notes = postAudit.notes;
        dataAccess.saveAudit(audit, "");

        response.setStatus(200);
        request.setHandled(true);
    }

}
