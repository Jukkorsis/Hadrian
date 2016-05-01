package com.northernwall.hadrian;

import java.util.Date;
import java.util.TimeZone;

public class GMT {

    public static Date getGmtAsDate() {
        return java.util.Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
    }
    
    public static long getGmtAsLong() {
        return getGmtAsDate().getTime();
    }
    
}
