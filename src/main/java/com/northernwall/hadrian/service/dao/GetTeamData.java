package com.northernwall.hadrian.service.dao;

import com.northernwall.hadrian.domain.Team;
import java.util.List;

public class GetTeamData {
    public String teamId;
    public String teamName;
    public String teamEmail;
    public String teamIrc;
    public String gitRepo;
    public String calendarId;
    public List<String> usernames;
    public boolean canModify;

    public static GetTeamData create(Team team) {
        GetTeamData temp = new GetTeamData();
        temp.teamId = team.getTeamId();
        temp.teamName = team.getTeamName();
        temp.teamEmail = team.getTeamEmail();
        temp.teamIrc = team.getTeamIrc();
        temp.gitRepo = team.getGitRepo();
        temp.calendarId = team.getCalendarId();
        temp.usernames = team.getUsernames();
        return temp;
    }

}
