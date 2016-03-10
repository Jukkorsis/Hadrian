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
import com.northernwall.hadrian.calendar.CalendarHelper;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.CalendarEntry;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.service.dao.GetCalendarData;
import com.northernwall.hadrian.utilityHandlers.routingHandler.Http404NotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class CalendarGetHandler extends AbstractHandler {

    private final DataAccess dataAccess;
    private final CalendarHelper calendarHelper;
    private final Gson gson;

    public CalendarGetHandler(DataAccess dataAccess, CalendarHelper calendarHelper) {
        this.dataAccess = dataAccess;
        this.calendarHelper = calendarHelper;
        gson = new Gson();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        String serviceId = request.getParameter("serviceId");

        GetCalendarData getCalendarData = new GetCalendarData();
        if (serviceId != null && !serviceId.isEmpty()) {
            Service service = dataAccess.getService(serviceId);
            if (service == null) {
                throw new Http404NotFoundException("Could not find service");
            }
            Team team = dataAccess.getTeam(service.getTeamId());
            if (team == null) {
                throw new Http404NotFoundException("Could not find team");
            }
            getCalendarData.entries = calendarHelper.getCalendarEntries(team);

            if (getCalendarData.entries == null) {
                getCalendarData.entries = new LinkedList<>();
            }
            if (getCalendarData.entries.isEmpty()) {
                CalendarEntry entry = new CalendarEntry();
                entry.calendarName = "-";
                entry.starts = "-";
                entry.ends = "-";
                entry.description = "-";
                getCalendarData.entries.add(entry);
            }
        }

        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(getCalendarData, GetCalendarData.class, jw);
        }

        response.setStatus(200);
        request.setHandled(true);
    }

}
