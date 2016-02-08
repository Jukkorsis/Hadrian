package com.northernwall.hadrian.calendar;

import com.northernwall.hadrian.parameters.Parameters;
import com.squareup.okhttp.OkHttpClient;

public interface CalendarHelperFactory {
    CalendarHelper create(Parameters parameters, OkHttpClient client);

}
