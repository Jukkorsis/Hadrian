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

import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.domain.User;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleSessionStore {
    private final static Logger LOGGER = LoggerFactory.getLogger(SimpleSessionStore.class);

    private final AccessHelper accessHelper;
    private final Map<String, SimpleSession> sessions;

    public SimpleSessionStore(AccessHelper accessHelper) {
        this.accessHelper = accessHelper;
        sessions = new HashMap<>();
    }

    public String createUserSession(String username) {
        SimpleSession session = new SimpleSession(username, System.currentTimeMillis() + Const.COOKIE_EXPRIY);
        sessions.put(session.getSessionId(), session);

        User user = accessHelper.getUser(username);

        LOGGER.info("login passed for {}, session {} started", user.getUsername(), session.getSessionId());
        return session.getSessionId();
    }

    public User getUserForSession(String sessionId) {
        SimpleSession session = sessions.get(sessionId);
        if (session == null) {
            LOGGER.warn("Could not find user sesion with ID {}", sessionId);
            return null;
        }
        if (session.getEndDateTime() < System.currentTimeMillis()) {
            LOGGER.warn("Trying to use session {} which has expried", sessionId);
            sessions.remove(sessionId);
            return null;
        }
        String username = session.getUsername();
        User user = accessHelper.getUser(username);
        return user;
    }

}
