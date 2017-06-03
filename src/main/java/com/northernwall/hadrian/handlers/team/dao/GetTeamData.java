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
package com.northernwall.hadrian.handlers.team.dao;

import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.sshAccess.SshEntry;
import java.util.List;

public class GetTeamData {
    public static GetTeamData create(Team team) {
        GetTeamData temp = new GetTeamData();
        temp.teamId = team.getTeamId();
        temp.teamName = team.getTeamName();
        temp.teamEmail = team.getTeamEmail();
        temp.teamSlack = team.getTeamSlack();
        temp.gitGroup = team.getGitGroup();
        temp.teamPage = team.getTeamPage();
        temp.colour = team.getColour();
        temp.securityGroupName = team.getSecurityGroupName();
        temp.sshEntries = team.getSshEntries();
        return temp;
    }
    
    public String teamId;
    public String teamName;
    public String teamEmail;
    public String teamSlack;
    public String gitGroup;
    public String teamPage;
    public String colour;
    public String securityGroupName;
    public List<SshEntry> sshEntries;
    public boolean canModify;

}
