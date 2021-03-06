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
package com.northernwall.hadrian.access.simple;

import java.util.UUID;

public class SimpleSession {
    private final String sessionId;
    private final String username;
    private final long endDateTime;

    public SimpleSession(String username, long endDateTime) {
        this.sessionId = UUID.randomUUID().toString();
        this.username = username;
        this.endDateTime = endDateTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

    public long getEndDateTime() {
        return endDateTime;
    }

}
