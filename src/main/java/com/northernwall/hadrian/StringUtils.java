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

/**
 *
 * @author Richard
 */
public class StringUtils {

    public static boolean same(String a, String b) {
        if (a == null || a.isEmpty()) {
            if (b == null || b.isEmpty()) {
                return true;
            } else {
                return false;
            }
        } else if (b == null || b.isEmpty()) {
            return false;
        } else {
            return a.equals(b);
        }
    }
    
    public static String dateRange(long ms) {
        long now = System.currentTimeMillis();
        long diff = (now - ms) / 60_000;
        if (diff <= 0) {
            return "0m";
        } else if (diff < 60) {
            return diff + "m";
        } else if (diff < (60*24)) {
            long minutes = diff % 60;
            long hours = diff / 60;
            return hours + "h " + minutes + "m";
        } else {
            diff = diff / 60;
            long hours = diff % 24;
            long days = diff / 24;
            return days + "d " + hours + "h";
        }
    }

    private StringUtils() {
    }

}
