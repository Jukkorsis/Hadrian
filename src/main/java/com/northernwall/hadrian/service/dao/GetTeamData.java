package com.northernwall.hadrian.service.dao;

import com.northernwall.hadrian.domain.Team;

public class GetTeamData {
    public String teamId;
    public String teamName;

    public static GetTeamData create(Team team) {
        GetTeamData temp = new GetTeamData();
        temp.teamId = team.getTeamId();
        temp.teamName = team.getTeamName();
        return temp;
    }

}
