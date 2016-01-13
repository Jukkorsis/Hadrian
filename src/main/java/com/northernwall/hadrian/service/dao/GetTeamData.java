package com.northernwall.hadrian.service.dao;

import com.northernwall.hadrian.domain.Team;
import java.util.List;

public class GetTeamData {
    public String teamId;
    public String teamAbbr;
    public String teamName;
    public String teamEmail;
    public String teamIrc;
    public List<String> usernames;
    public boolean canModify;

    public static GetTeamData create(Team team) {
        GetTeamData temp = new GetTeamData();
        temp.teamId = team.getTeamId();
        temp.teamAbbr = team.getTeamAbbr();
        temp.teamName = team.getTeamName();
        temp.teamEmail = team.getTeamEmail();
        temp.teamIrc = team.getTeamIrc();
        temp.usernames = team.getUsernames();
        return temp;
    }

}
