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
import com.northernwall.hadrian.handlers.utility.routingHandler.Http401UnauthorizedException;
import com.northernwall.hadrian.handlers.utility.routingHandler.Http404NotFoundException;

import java.util.List;

import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessHelper {

    private final static Logger LOGGER = LoggerFactory.getLogger(AccessHelper.class);

    private final DataAccess dataAccess;

    public AccessHelper(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public User getUser(String username) {
        User user = dataAccess.getUser(username);
        if (user == null) {
            List<User> users = dataAccess.getUsers();
            if (users == null || users.isEmpty()) {
                LOGGER.info("No users found. So creating {} as the first user", username);
                user = new User(username, username, true, false, false);
            } else {
                LOGGER.info("User {} not found, creating", username);
                user = new User(username, username, false, false, false);
            }
            dataAccess.saveUser(user);
        }
        return user;
    }

    public boolean canUserModify(Request request, String teamId) {
        User user = (User) request.getAttribute(Const.ATTR_USER);
        if (user == null) {
            throw new Http404NotFoundException("unknown user");
        }
        Team team = dataAccess.getTeam(teamId);
        if (team == null) {
            throw new Http404NotFoundException("unknown team");
        }
        return team.getUsernames().contains(user.getUsername());
    }

    public User checkIfUserCanModify(Request request, String teamId, String action) {
        User user = (User) request.getAttribute(Const.ATTR_USER);
        if (user == null) {
            throw new Http404NotFoundException("unknown user attempted to " + action);
        }
        String username = user.getUsername();
        Team team = dataAccess.getTeam(teamId);
        if (team == null) {
            throw new Http404NotFoundException(username + " attempted to " + action + " on team " + teamId + " but could not find team");
        }
        if (!team.getUsernames().contains(username)) {
            throw new Http401UnauthorizedException(username + " attempted to " + action + " on team " + team.getTeamName());
        }
        return user;
    }

    public User checkIfUserCanDeploy(Request request, String teamId) {
        User user = (User) request.getAttribute(Const.ATTR_USER);
        if (user == null) {
            throw new Http404NotFoundException("unknown user attempted to deploy software to host");
        }
        if (user.isDeploy()) {
            return user;
        }
        String username = user.getUsername();
        Team team = dataAccess.getTeam(teamId);
        if (team == null) {
            throw new Http404NotFoundException(username + " attempted to deploy software to host on team " + teamId + " but could not find team");
        }
        if (!team.getUsernames().contains(username)) {
            throw new Http401UnauthorizedException(username + " attempted to deploy software to host on team " + team.getTeamName());
        }
        return user;
    }

    public User checkIfUserCanRestart(Request request, String teamId) {
        User user = (User) request.getAttribute(Const.ATTR_USER);
        if (user == null) {
            throw new Http404NotFoundException("unknown user attempted to restart host");
        }
        if (user.isDeploy() || user.isAdmin()) {
            return user;
        }
        String username = user.getUsername();
        Team team = dataAccess.getTeam(teamId);
        if (team == null) {
            throw new Http404NotFoundException(username + " attempted to restart host on team " + teamId + " but could not find team");
        }
        if (!team.getUsernames().contains(username)) {
            throw new Http401UnauthorizedException(username + " attempted to restart host on team " + team.getTeamName());
        }
        return user;
    }

    public User checkIfUserCanAudit(Request request, Team team) {
        User user = (User) request.getAttribute(Const.ATTR_USER);
        if (user == null) {
            throw new Http404NotFoundException("unknown user attempted to add audit record");
        }
        if (user.isAudit()) {
            return user;
        }
        String username = user.getUsername();
        if (!team.getUsernames().contains(username)) {
            throw new Http401UnauthorizedException(username + " attempted to add audit record on team " + team.getTeamName());
        }
        return user;
    }

    public User checkIfUserCanAudit(Request request, String teamId) {
        User user = (User) request.getAttribute(Const.ATTR_USER);
        if (user == null) {
            throw new Http404NotFoundException("unknown user attempted to add audit record");
        }
        if (user.isAudit()) {
            return user;
        }
        String username = user.getUsername();
        Team team = dataAccess.getTeam(teamId);
        if (team == null) {
            throw new Http404NotFoundException(username + " attempted to add audit record on team " + teamId + " but could not find team");
        }
        if (!team.getUsernames().contains(username)) {
            throw new Http401UnauthorizedException(username + " attempted to add audit record on team " + team.getTeamName());
        }
        return user;
    }

    public User checkIfUserIsAdmin(Request request, String action) {
        User user = (User) request.getAttribute(Const.ATTR_USER);
        if (user == null) {
            throw new Http404NotFoundException("unknown users attempted to " + action + " but is not an admin");
        }
        if (!user.isAdmin()) {
            throw new Http401UnauthorizedException(user.getUsername() + " attempted to " + action + " but is not an admin");
        }
        return user;
    }

}
