/*
 * Copyright 2015 Richard Thurston.
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

import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author Richard Thurston
 */
public class Team implements Comparable<Team> {
    private String teamId;
    private String teamName;
    private String teamEmail;
    private String teamSlack;
    private String gitGroup;
    private String teamPage;
    private String calendarId;
    private String colour;
    private String securityGroupName;

    public Team(String teamName, String teamEmail, String teamSlack, String gitGroup, String teamPage, String calendarId, String colour, String securityGroupName) {
        this.teamId = UUID.randomUUID().toString();
        this.teamName = teamName;
        this.teamEmail = teamEmail;
        this.teamSlack = teamSlack;
        this.gitGroup = gitGroup;
        this.teamPage = teamPage;
        this.calendarId = calendarId;
        this.colour = colour;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamEmail() {
        return teamEmail;
    }

    public void setTeamEmail(String teamEmail) {
        this.teamEmail = teamEmail;
    }

    public String getTeamSlack() {
        return teamSlack;
    }

    public void setTeamSlack(String teamSlack) {
        this.teamSlack = teamSlack;
    }

    public String getGitGroup() {
        return gitGroup;
    }

    public void setGitGroup(String gitGroup) {
        this.gitGroup = gitGroup;
    }

    public String getTeamPage() {
        return teamPage;
    }

    public void setTeamPage(String teamPage) {
        this.teamPage = teamPage;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }

    public String getColour() {
        if (colour == null || colour.isEmpty()) {
            return "black";
        }
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public String getSecurityGroupName() {
        if (securityGroupName == null || securityGroupName.isEmpty()) {
            return teamName;
        }
        return securityGroupName;
    }

    public void setSecurityGroupName(String securityGroupName) {
        this.securityGroupName = securityGroupName;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.teamId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Team other = (Team) obj;
        if (!Objects.equals(this.teamId, other.teamId)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Team o) {
        return teamName.compareTo(o.teamName);
    }

}
