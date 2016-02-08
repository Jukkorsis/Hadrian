package com.northernwall.hadrian.service;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.calendar.CalendarHelper;
import com.northernwall.hadrian.domain.CalendarEntry;
import com.northernwall.hadrian.service.dao.GetCalendarData;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarHandler extends AbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(AuditHandler.class);
    
    private final CalendarHelper calendarHelper;
    private final Gson gson;

    public CalendarHandler(CalendarHelper calendarHelper) {
        this.calendarHelper = calendarHelper;
        gson = new Gson();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.startsWith("/v1/calendar") && request.getMethod().equals(Const.HTTP_GET)) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                getCalendar(response);
                response.setStatus(200);
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void getCalendar(HttpServletResponse response) throws IOException {        
        GetCalendarData getCalendarData = new GetCalendarData();
        getCalendarData.entries = calendarHelper.getCalendarEntries();
        
        if (getCalendarData.entries ==null) {
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

        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(getCalendarData, GetCalendarData.class, jw);
        }
    }

}
