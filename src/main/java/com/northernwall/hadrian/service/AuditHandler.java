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

import com.northernwall.hadrian.Util;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.service.dao.PostAudit;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http400BadRequestException;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http404NotFoundException;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(AuditHandler.class);

    private final DataAccess dataAccess;

    public AuditHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        PostAudit postAudit = Util.fromJson(request, PostAudit.class);

        Service service = findService(postAudit);

        Audit audit = new Audit();
        audit.serviceId = service.getServiceId();
        audit.timePerformed = Util.getGmt();
        audit.timeRequested = Util.getGmt();
        audit.requestor = postAudit.username;
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

    private Service findService(PostAudit postAudit) {
        List<Service> services = dataAccess.getServices();
        for (Service service : services) {
            if (postAudit.serviceId != null && !postAudit.serviceId.isEmpty() && service.getServiceId().equals(postAudit.serviceId)) {
                return service;
            }
            if (postAudit.serviceAbbr != null && !postAudit.serviceAbbr.isEmpty() && service.getServiceAbbr().equals(postAudit.serviceAbbr)) {
                return service;
            }
            if (postAudit.serviceName != null && !postAudit.serviceName.isEmpty() && service.getServiceName().equals(postAudit.serviceName)) {
                return service;
            }
        }
        if (postAudit.serviceId != null && !postAudit.serviceId.isEmpty()) {
            throw new Http404NotFoundException("Could not find service "+postAudit.serviceId+", so can not record audit");
        }
        if (postAudit.serviceAbbr != null && !postAudit.serviceAbbr.isEmpty()) {
            throw new Http404NotFoundException("Could not find service "+postAudit.serviceAbbr+", so can not record audit");
        }
        if (postAudit.serviceName != null && !postAudit.serviceId.isEmpty()) {
            throw new Http404NotFoundException("Could not find service "+postAudit.serviceName+", so can not record audit");
        }
        throw new Http400BadRequestException("No service Id, abbr, or name provided");
    }

}
