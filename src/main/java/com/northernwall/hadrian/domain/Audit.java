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
package com.northernwall.hadrian.domain;

import java.util.Date;

public class Audit implements Comparable<Audit> {
    public String auditId;
    public String serviceId;
    public Date timeRequested;
    private Date timePerformed;
    private long timePerformedLong = 0;
    public String requestor;
    public Type type;
    public Operation operation;
    public boolean successfull = true;
    public String moduleName;
    public String hostName;
    public String vipName;
    
    /**
     * notes is a string which contains a json encoded Map of attributes (String) and values (String).
     */
    public String notes;

    public Date getTimePerformed() {
        return timePerformed;
    }

    public void setTimePerformed(Date timePerformed) {
        this.timePerformed = timePerformed;
        this.timePerformedLong = timePerformed.getTime();
    }

    @Override
    public int compareTo(Audit o) {
        int result = o.timePerformed.compareTo(timePerformed);
        if (result != 0) {
            return result;
        }
        if (timePerformedLong > 0 && o.timePerformedLong > 0) {
            result = (int)(o.timePerformedLong - timePerformedLong);
            if (result != 0) {
                return result;
            }
        }
        result = o.timeRequested.compareTo(timeRequested);
        if (result != 0) {
            return result;
        }
        return o.serviceId.compareTo(serviceId);
    }

}
