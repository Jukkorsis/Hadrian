/*
 * Copyright 2017 Richard Thurston.
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

import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author Richard
 */
public class DateRangeTest {
    
    public DateRangeTest() {
    }

    @Test
    public void futureTest() {
        long now = System.currentTimeMillis() + 100_000;
        Assert.assertEquals("0m", StringUtils.dateRange(now));
    }

    @Test
    public void nowTest() {
        long now = System.currentTimeMillis();
        Assert.assertEquals("0m", StringUtils.dateRange(now));
    }

    @Test
    public void minutesTest() {
        long now = System.currentTimeMillis();
        long then = now - (2 * 60 * 1000) - 500;
        Assert.assertEquals("2m", StringUtils.dateRange(then));
    }

    @Test
    public void hoursAndMinutesTest() {
        long now = System.currentTimeMillis();
        long then = now - (4 * 60 * 60 * 1000) - (15 * 60 * 1000) - 500;
        Assert.assertEquals("4h 15m", StringUtils.dateRange(then));
    }

    @Test
    public void daysAndHoursTest() {
        long now = System.currentTimeMillis();
        long then = now - (3 * 24 * 60 * 60 * 1000) - (23 * 60 * 60 * 1000) - (15 * 60 * 1000) - 500;
        Assert.assertEquals("3d 23h", StringUtils.dateRange(then));
    }

}
