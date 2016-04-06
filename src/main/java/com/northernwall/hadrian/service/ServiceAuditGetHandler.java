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

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.service.dao.GetAuditData;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class ServiceAuditGetHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(ServiceAuditGetHandler.class);

    private final DataAccess dataAccess;
    private final Gson gson;
    private final DateFormat format;

    public ServiceAuditGetHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        gson = new Gson();

        format = new SimpleDateFormat("MM/dd/yyyy");
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String start = request.getParameter("start");
        String end = request.getParameter("end");
        String id = request.getParameter("serviceId");
        GetAuditData auditData = new GetAuditData();

        Date startDate = null;
        try {
            startDate = format.parse(start);
        } catch (ParseException ex) {
            Calendar now = Calendar.getInstance();
            now.add(Calendar.DATE, -15);
            now.clear(Calendar.HOUR);
            now.clear(Calendar.MINUTE);
            now.clear(Calendar.SECOND);
            startDate = now.getTime();
        }
        Date endDate = null;
        try {
            endDate = format.parse(end);
        } catch (ParseException ex) {
            Calendar now = Calendar.getInstance();
            now.add(Calendar.DATE, 1);
            now.clear(Calendar.HOUR);
            now.clear(Calendar.MINUTE);
            now.clear(Calendar.SECOND);
            endDate = now.getTime();
        }
        logger.info("Audit search from {} to {} on service {}", startDate.toString(), endDate.toString(), id);
        auditData.audits = dataAccess.getAudit(id, startDate, endDate);
        Collections.sort(auditData.audits);

        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(auditData, GetAuditData.class, jw);
        }
        response.setStatus(200);
        request.setHandled(true);
    }

}
