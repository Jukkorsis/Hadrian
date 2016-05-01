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
package com.northernwall.hadrian.calendar.google;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.GMT;
import com.northernwall.hadrian.calendar.CalendarHelper;
import com.northernwall.hadrian.domain.CalendarEntry;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.parameters.Parameters;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleCalendarHelper extends CalendarHelper {

    private final static Logger logger = LoggerFactory.getLogger(GoogleCalendarHelper.class);

    private final Calendar calendarClient;
    private final Parameters parameters;

    public GoogleCalendarHelper(Calendar calendarClient, Parameters parameters) {
        this.calendarClient = calendarClient;
        this.parameters = parameters;
    }

    @Override
    public List<CalendarEntry> getCalendarEntries(Team team) {
        List<CalendarEntry> entries = new LinkedList<>();
        
        String calendarId = parameters.getString(Const.CALENDAR_GOOGLE_GLOBAL_ID, null);
        if (calendarId != null) {
            getEntriesForCalendar(calendarId, entries);
        }
        
        if (team != null && team.getCalendarId() != null) {
            getEntriesForCalendar(team.getCalendarId(), entries);
        }
        
        Collections.sort(entries);
        return entries;
    }

    private void getEntriesForCalendar(String calendarId, List<CalendarEntry> entries) {
        long now = GMT.getGmtAsLong();
        try {
            DateTime timeMin = new DateTime(now, 0);
            DateTime timeMax = new DateTime(now + ONE_DAY + ONE_HOUR, 0);
            Events events = calendarClient.events()
                    .list(calendarId)
                    //.setTimeZone("Europe/London")
                    //.setTimeZone("UTC")
                    .setTimeMin(timeMin)
                    .setTimeMax(timeMax)
                    .execute();
            List<Event> eventsList = events.getItems();
            if (eventsList != null && !eventsList.isEmpty()) {
                for (Event event : eventsList) {
                    if (event.getRecurrence() == null || event.getRecurrence().isEmpty()) {
                        //The event is a single instance
                        processEvent(event,  now, entries);
                    } else {
                        //The event is a recurring event, so we must load the different instances
                        Events recurringEvents = calendarClient.events()
                                .instances(calendarId, event.getId())
                                //.setTimeZone("Europe/London")
                                //.setTimeZone("UTC")
                                .setTimeMin(timeMin)
                                .setTimeMax(timeMax)
                                .execute();
                        List<Event> recurringEventsList = recurringEvents.getItems();
                        if (recurringEventsList != null && !recurringEventsList.isEmpty()) {
                            for (Event recurringEvent : recurringEventsList) {
                                processEvent(recurringEvent,  now, entries);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            logger.warn("IO Exception while getting calendar entries, {}", ex.getMessage());
        }
    }
    
    private void processEvent(Event event, long now, List<CalendarEntry> entries) {
        long start = getDateTime(event.getStart());
        long end = getDateTime(event.getEnd());
        if (end != 0 && start != 0 && end > now) {
            CalendarEntry entry = new CalendarEntry();
            entry.calendarName = event.getOrganizer().getDisplayName();
            entry.startTime = start;
            entry.starts = buildStartsEndsText(start);
            entry.ends = buildStartsEndsText(end);
            entry.description = event.getSummary();
            entries.add(entry);
        }
    }

    private long getDateTime(EventDateTime eventDateTime) {
        if (eventDateTime.getDateTime() != null) {
            return eventDateTime.getDateTime().getValue();
        }
        if (eventDateTime.getDate() != null) {
            return eventDateTime.getDate().getValue();
        }
        return 0;
    }

}
