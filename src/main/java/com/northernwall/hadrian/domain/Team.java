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

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Richard Thurston
 */
public class Team implements Comparable<Team> {
    private String teamId;
    private String teamName;
    private String teamEmail;
    private String teamIrc;
    private String teamSlack;
    private String gitRepo;
    private String gitGroup;
    private String calendarId;
    private List<String> usernames;

    public Team(String teamName, String teamEmail, String teamIrc, String teamSlack, String gitGroup, String calendarId) {
        this.teamId = UUID.randomUUID().toString();
        this.teamName = teamName;
        this.teamEmail = teamEmail;
        this.teamIrc = teamIrc;
        this.teamSlack = teamSlack;
        this.gitRepo = gitGroup;
        this.gitGroup = gitGroup;
        this.calendarId = calendarId;
        this.usernames = new LinkedList<>();
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

    public String getTeamIrc() {
        return teamIrc;
    }

    public void setTeamIrc(String teamIrc) {
        this.teamIrc = teamIrc;
    }

    public String getTeamSlack() {
        return teamSlack;
    }

    public void setTeamSlack(String teamSlack) {
        this.teamSlack = teamSlack;
    }

    public String getGitRepo() {
        return gitRepo;
    }

    public void setGitRepo(String gitRepo) {
        this.gitRepo = gitRepo;
    }

    public String getGitGroup() {
        return gitGroup;
    }

    public void setGitGroup(String gitGroup) {
        this.gitGroup = gitGroup;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }

    public List<String> getUsernames() {
        return usernames;
    }

    @Override
    public int compareTo(Team o) {
        return teamName.compareTo(o.teamName);
    }

}
