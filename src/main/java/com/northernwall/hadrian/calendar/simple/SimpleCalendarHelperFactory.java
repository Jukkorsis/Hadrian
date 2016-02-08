package com.northernwall.hadrian.calendar.simple;

import com.northernwall.hadrian.calendar.CalendarHelper;
import com.northernwall.hadrian.calendar.CalendarHelperFactory;
import com.northernwall.hadrian.parameters.Parameters;
import com.squareup.okhttp.OkHttpClient;

public class SimpleCalendarHelperFactory implements CalendarHelperFactory {

    @Override
    public CalendarHelper create(Parameters parameters, OkHttpClient client) {
        return new SimpleCalendarHelper();
    }

}
