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

public class CalendarGetHandler extends BasicHandler {

    private final CalendarHelper calendarHelper;

    public CalendarGetHandler(DataAccess dataAccess, CalendarHelper calendarHelper) {
        super(dataAccess);
        this.calendarHelper = calendarHelper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        Service service = getService(request);
        Team team = getDataAccess().getTeam(service.getTeamId());

        GetCalendarData data = new GetCalendarData();
        data.entries = calendarHelper.getCalendarEntries(team);

        if (data.entries == null) {
            data.entries = new LinkedList<>();
        }
        if (data.entries.isEmpty()) {
            CalendarEntry entry = new CalendarEntry();
            entry.calendarName = "-";
            entry.starts = "-";
            entry.ends = "-";
            entry.description = "-";
            data.entries.add(entry);
        }

        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            getGson().toJson(data, GetCalendarData.class, jw);
        }

        response.setStatus(200);
        request.setHandled(true);
    }

}
