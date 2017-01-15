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

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.handlers.routing.Http401UnauthorizedException;
import com.northernwall.hadrian.handlers.routing.Http404NotFoundException;
import com.northernwall.hadrian.parameters.Parameters;

import org.eclipse.jetty.server.Request;

public class SimpleAccessHelper implements AccessHelper {

    private final Parameters parameters;

    public SimpleAccessHelper(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public User getUser(String username) {
        return new User(username);
    }

    @Override
    public boolean canUserModify(Request request, Team team) {
        User user = (User) request.getAttribute(Const.ATTR_USER);
        if (user == null) {
            throw new Http404NotFoundException("unknown user");
        }
        return isInGroup(user, "group.team."+team.getSecurityGroupName());
    }

    @Override
    public User checkIfUserCanModify(Request request, Team team, String action) {
        User user = (User) request.getAttribute(Const.ATTR_USER);
        if (user == null) {
            throw new Http404NotFoundException("unknown user attempted to " + action);
        }
        String username = user.getUsername();
        if (!isInGroup(user, "group.team."+team.getSecurityGroupName())) {
            throw new Http401UnauthorizedException(username + " attempted to " + action + " on team " + team.getTeamName());
        }
        return user;
    }

    @Override
    public User checkIfUserCanDeploy(Request request, Team team) {
        User user = (User) request.getAttribute(Const.ATTR_USER);
        if (user == null) {
            throw new Http404NotFoundException("unknown user attempted to deploy software to host");
        }
        if (isInGroup(user, "group.deploy")) {
            return user;
        }
        String username = user.getUsername();
        if (!isInGroup(user, "group.team."+team.getSecurityGroupName())) {
            throw new Http401UnauthorizedException(username + " attempted to deploy software to host on team " + team.getTeamName());
        }
        return user;
    }

    @Override
    public User checkIfUserCanRestart(Request request, Team team) {
        User user = (User) request.getAttribute(Const.ATTR_USER);
        if (user == null) {
            throw new Http404NotFoundException("unknown user attempted to restart host");
        }
        if (isInGroup(user, "group.deploy") || isInGroup(user, "group.admin")) {
            return user;
        }
        String username = user.getUsername();
        if (!isInGroup(user, "group.team."+team.getSecurityGroupName())) {
            throw new Http401UnauthorizedException(username + " attempted to restart host on team " + team.getTeamName());
        }
        return user;
    }

    @Override
    public User checkIfUserCanAudit(Request request, Team team) {
        User user = (User) request.getAttribute(Const.ATTR_USER);
        if (user == null) {
            throw new Http404NotFoundException("unknown user attempted to add audit record");
        }
        if (isInGroup(user, "group.audit")) {
            return user;
        }
        String username = user.getUsername();
        if (!isInGroup(user, "group.team."+team.getSecurityGroupName())) {
            throw new Http401UnauthorizedException(username + " attempted to add audit record on team " + team.getTeamName());
        }
        return user;
    }
    
    @Override
    public boolean isAdmin(Request request, String action) {
        User user = (User) request.getAttribute(Const.ATTR_USER);
        if (user == null) {
            throw new Http404NotFoundException("unknown users attempted to " + action + " but is not an admin");
        }
        return isInGroup(user, "group.admin");
    }

    @Override
    public User checkIfUserIsAdmin(Request request, String action) {
        User user = (User) request.getAttribute(Const.ATTR_USER);
        if (user == null) {
            throw new Http404NotFoundException("unknown users attempted to " + action + " but is not an admin");
        }
        if (!isInGroup(user, "group.admin")) {
            throw new Http401UnauthorizedException(user.getUsername() + " attempted to " + action + " but is not an admin");
        }
        return user;
    }
    
    private boolean isInGroup(User user, String groupName) {
        String parameter = parameters.getString(groupName, null);
        if (parameter == null || parameter.isEmpty()) {
            return false;
        }
        String[] userNames = parameter.split(",");
        for (String userName : userNames) {
            if (userName.equalsIgnoreCase(user.getUsername())) {
                return true;
            }
        }
        return false;
    }

}
