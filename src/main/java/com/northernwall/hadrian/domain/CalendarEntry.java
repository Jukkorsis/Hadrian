package com.northernwall.hadrian.domain;

public class CalendarEntry implements Comparable<CalendarEntry> {
    public String calendarName;
    public long startTime;
    public String starts;
    public String ends;
    public String description;

    @Override
    public int compareTo(CalendarEntry otherEntry) {
        return (int)((startTime - otherEntry.startTime) / 1_000);
    }

}
