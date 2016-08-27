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
package com.northernwall.hadrian.handlers.service;

import com.northernwall.hadrian.handlers.BasicHandler;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.handlers.service.dao.GetAuditData;
import com.northernwall.hadrian.handlers.utility.routingHandler.Http400BadRequestException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
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
public class AuditGetHandler extends BasicHandler {

    private final static Logger logger = LoggerFactory.getLogger(AuditGetHandler.class);

    public AuditGetHandler(DataAccess dataAccess) {
        super(dataAccess);
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String start = request.getParameter("start");
        String end = request.getParameter("end");
        String id = request.getParameter("serviceId");
        GetAuditData auditData = new GetAuditData();

        Service service = getService(request);
        if (year == null || year.isEmpty()) {
            throw new Http400BadRequestException("parameter year is missing");
        }
        if (month == null || month.isEmpty()) {
            throw new Http400BadRequestException("parameter month is missing");
        }
        if (start == null || start.isEmpty()) {
            throw new Http400BadRequestException("parameter start is missing");
        }
        if (end == null || end.isEmpty()) {
            throw new Http400BadRequestException("parameter end is missing");
        }
        
        auditData.audits = getDataAccess().getAudit(
                id, 
                Integer.parseInt(year), 
                Integer.parseInt(month), 
                Integer.parseInt(start), 
                Integer.parseInt(end));
        logger.info("Got {} audit record for {} between {} {} {} and {}", 
                auditData.audits.size(), 
                service.getServiceName(), 
                year, 
                month, 
                start, 
                end);
        Collections.sort(auditData.audits);

        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            getGson().toJson(auditData, GetAuditData.class, jw);
        }
        response.setStatus(200);
        request.setHandled(true);
    }

}
