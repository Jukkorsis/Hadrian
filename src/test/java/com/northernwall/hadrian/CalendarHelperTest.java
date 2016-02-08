/*
 * Copyright 2016 Richard Thurston.
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
package com.northernwall.hadrian;

import com.northernwall.hadrian.calendar.CalendarHelper;
import org.junit.Test;
import org.junit.Assert;

/**
 *
 * @author rthursto
 */
public class CalendarHelperTest {

    public CalendarHelperTest() {
    }

    @Test
    public void buildStartEndTextTest() {
        long time = System.currentTimeMillis() - (2 * CalendarHelper.ONE_DAY) - CalendarHelper.ONE_HOUR;
        Assert.assertEquals("2 days ago", CalendarHelper.buildStartsEndsText(time));

        time = System.currentTimeMillis() - (4 * CalendarHelper.ONE_HOUR) - (18 * CalendarHelper.ONE_MINUTE);
        Assert.assertEquals("4 hours ago", CalendarHelper.buildStartsEndsText(time));

        time = System.currentTimeMillis() - (25 * CalendarHelper.ONE_MINUTE);
        Assert.assertEquals("25 minutes ago", CalendarHelper.buildStartsEndsText(time));

        time = System.currentTimeMillis() + (1 * CalendarHelper.ONE_DAY) + CalendarHelper.ONE_HOUR;
        Assert.assertEquals("in 1 day", CalendarHelper.buildStartsEndsText(time));

        time = System.currentTimeMillis() + (5 * CalendarHelper.ONE_HOUR) - (12 * CalendarHelper.ONE_MINUTE);
        Assert.assertEquals("in 4 hours", CalendarHelper.buildStartsEndsText(time));

        time = System.currentTimeMillis() + (34 * CalendarHelper.ONE_MINUTE);
        Assert.assertEquals("in 34 minutes", CalendarHelper.buildStartsEndsText(time));

    }

}
