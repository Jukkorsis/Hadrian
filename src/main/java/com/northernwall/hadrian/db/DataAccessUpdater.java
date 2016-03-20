package com.northernwall.hadrian.db;

import com.northernwall.hadrian.domain.Team;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAccessUpdater {

    private final static Logger logger = LoggerFactory.getLogger(DataAccessUpdater.class);

    public static void update(DataAccess dataAccess) {
        List<Team> teams = dataAccess.getTeams();
        for (Team team : teams) {
            if (team.getGitGroup() == null || team.getGitGroup().isEmpty()) {
                team.setGitGroup(team.getGitRepo());
                dataAccess.saveTeam(team);
                logger.info("Upgrading team {} to have Git Group '{}'", team.getTeamName(), team.getGitGroup());
            }
        }
    }

    private DataAccessUpdater() {
    }
}
