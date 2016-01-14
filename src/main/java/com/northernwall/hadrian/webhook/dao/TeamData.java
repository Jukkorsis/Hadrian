package com.northernwall.hadrian.webhook.dao;

import com.northernwall.hadrian.domain.Team;

public class TeamData {
    public String teamAbbr;
    public String teamName;
    public String teamEmail;
    public String teamIrc;

    public static TeamData create(Team team) {
        if (team == null) {
            return null;
        }
        TeamData temp = new TeamData();
        temp.teamAbbr = team.getTeamAbbr();
        temp.teamName = team.getTeamName();
        temp.teamEmail = team.getTeamEmail();
        temp.teamIrc = team.getTeamIrc();
        return temp;
    }

}
