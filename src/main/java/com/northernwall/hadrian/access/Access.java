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
package com.northernwall.hadrian.access;

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.UserSession;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rthursto
 */
public abstract class Access {

    private final static Logger logger = LoggerFactory.getLogger(Access.class);

    protected final DataAccess dataAccess;

    public Access(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public abstract String checkAndStartSession(Request request, HttpServletResponse response) throws IOException;

    public final User getUserForSession(String sessionId) {
        UserSession session = dataAccess.getUserSession(sessionId);
        if (session == null) {
            logger.warn("Could not find user sesion with ID {}", sessionId);
            return null;
        }
        String username = session.getUsername();
        User user = dataAccess.getUser(username);
        if (user == null) {
            logger.info("User {} not found, creating", username);
            user = new User(username, username, false, false);
            dataAccess.saveUser(user);
        }
        return user;
    }

    public abstract void redirect(HttpServletResponse response) throws XMLStreamException, IOException;

    public final boolean canUserModify(Request request, String teamId) {
        Team team = dataAccess.getTeam(teamId);
        User user = (User) request.getAttribute(Const.ATTR_USER);
        return team.getUsernames().contains(user.getUsername());
    }

    public final void checkIfUserCanModify(Request request, String teamId, String action) {
        Team team = dataAccess.getTeam(teamId);
        User user = (User) request.getAttribute(Const.ATTR_USER);
        String username = user.getUsername();
        if (!team.getUsernames().contains(username)) {
            throw new AccessException(username + " attempted to " + action + " on team " + team.getTeamName());
        }
    }
    
}
