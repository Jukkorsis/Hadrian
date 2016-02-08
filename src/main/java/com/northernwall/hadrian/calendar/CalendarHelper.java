package com.northernwall.hadrian.calendar;

import com.northernwall.hadrian.domain.CalendarEntry;
import java.util.Date;
import java.util.List;

public abstract class CalendarHelper {
    
    public static final long ONE_MINUTE = 60*1000;
    public static final long ONE_HOUR = 60*ONE_MINUTE;
    public static final long ONE_DAY = 24*ONE_HOUR;
    public static final long MINUS_ONE_MINUTE = -1 * ONE_MINUTE;
    public static final long MINUS_ONE_HOUR = -1 * ONE_HOUR;
    public static final long MINUS_ONE_DAY = -1 * ONE_DAY;

    public abstract List<CalendarEntry> getCalendarEntries();
    
    public static String buildStartsEndsText(Date date) {
        return buildStartsEndsText(date.getTime());
    }
    
    public static String buildStartsEndsText(long date) {
        long dif = date - System.currentTimeMillis();
        
        if (dif < MINUS_ONE_DAY) {
            dif = dif / MINUS_ONE_DAY;
            if (dif == -1) {
                return "1 day ago";
            }
            return dif + " days ago";
        } else if (dif < MINUS_ONE_HOUR) {
            dif = dif / MINUS_ONE_HOUR;
            if (dif == -1) {
                return "1 hour ago";
            }
            return dif + " hours ago";
        } else if (dif <= 0) {
            dif = dif / MINUS_ONE_MINUTE;
            if (dif == -1) {
                return "1 minute ago";
            }
            return dif + " minutes ago";
        } else if (dif > ONE_DAY) {
            dif = dif / ONE_DAY;
            if (dif == 1) {
                return "in 1 day";
            }
            return "in " +dif + " days";
        } else if (dif > ONE_HOUR) {
            dif = dif / ONE_HOUR;
            if (dif == 1) {
                return "in 1 hour";
            }
            return "in " + dif + " hours";
        } else {
            dif = dif / ONE_MINUTE;
            if (dif == 1) {
                return "in 1 minute";
            }
            return "in " + dif + " minutes";
        }
    }
    
}
