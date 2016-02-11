package com.northernwall.hadrian.calendar.simple;

import com.northernwall.hadrian.calendar.CalendarHelper;
import com.northernwall.hadrian.domain.CalendarEntry;
import com.northernwall.hadrian.domain.Team;
import java.util.LinkedList;
import java.util.List;

public class SimpleCalendarHelper extends CalendarHelper {

    @Override
    public List<CalendarEntry> getCalendarEntries(Team team) {
        return new LinkedList<>();
    }

}
