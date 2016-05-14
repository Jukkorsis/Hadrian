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
package com.northernwall.hadrian.service.dao;

import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.User;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GetTeamData {
    public String teamId;
    public String teamName;
    public String teamEmail;
    public String teamIrc;
    public String teamSlack;
    public String gitGroup;
    public String calendarId;
    public List<User> users;
    public List<GetServiceData> services;
    public boolean canModify;

    public static GetTeamData create(Team team, DataAccess dataAccess) {
        GetTeamData temp = new GetTeamData();
        temp.teamId = team.getTeamId();
        temp.teamName = team.getTeamName();
        temp.teamEmail = team.getTeamEmail();
        temp.teamIrc = team.getTeamIrc();
        temp.teamSlack = team.getTeamSlack();
        temp.gitGroup = team.getGitGroup();
        temp.calendarId = team.getCalendarId();
        temp.users = new LinkedList<>();
        for (String username : team.getUsernames()) {
            temp.users.add(dataAccess.getUser(username));
        }
        Collections.sort(temp.users);
        temp.services = new LinkedList<>();
        for (Service service : dataAccess.getServices(team.getTeamId())) {
            temp.services.add(GetServiceData.create(service));
        }
        for (Service service : dataAccess.getDeletedServices(team.getTeamId())) {
            temp.services.add(GetServiceData.create(service));
        }
        return temp;
    }

}
