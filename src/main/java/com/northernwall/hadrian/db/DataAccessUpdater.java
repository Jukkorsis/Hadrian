package com.northernwall.hadrian.db;

import com.northernwall.hadrian.domain.ModuleFile;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.Team;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAccessUpdater {

    private final static Logger logger = LoggerFactory.getLogger(DataAccessUpdater.class);

    public static void update(DataAccess dataAccess) {
        String version = dataAccess.getVersion();

        if (version == null || version.equals("1.3")) {
            logger.info("Upgrading to 1.4 from {}", version);
            List<Team> teams = dataAccess.getTeams();
            if (teams != null && !teams.isEmpty()) {
                for (Team team : teams) {
                    if (team.getGitGroup() == null || team.getGitGroup().isEmpty()) {
                        team.setGitGroup(team.getGitRepo());
                        dataAccess.saveTeam(team);
                        logger.info("Upgrading team {} to have Git Group '{}'", team.getTeamName(), team.getGitGroup());
                    }
                }
            }
            dataAccess.setVersion("1.4");
            update(dataAccess);
        } else if (version.equals("1.4")) {
            logger.info("Upgrading to 1.5 from {}", version);
            List<Service> services = dataAccess.getAllServices();
            if (services != null && !services.isEmpty()) {
                for (Service service : services) {
                    service.setActive(true);
                    dataAccess.saveService(service);
                }
            }
            dataAccess.setVersion("1.5");
            update(dataAccess);
        } else {
            logger.info("Current version is {}, no upgrade required.", version);
            List<Service> services = dataAccess.getAllServices();
            if (services != null && !services.isEmpty()) {
                for (Service service : services) {
                    List<ModuleFile> moduleFiles = dataAccess.getModuleFiles(service.getServiceId());
                    if (moduleFiles != null && !moduleFiles.isEmpty()) {
                        for (ModuleFile moduleFile : moduleFiles) {
                            if (moduleFile.getNetwork().equalsIgnoreCase("Prod")
                                    || moduleFile.getNetwork().equalsIgnoreCase("Test")
                                    || moduleFile.getNetwork().equalsIgnoreCase("Reg")) {
                                logger.info("MF: {} {} {}", service.getServiceAbbr(), moduleFile.getNetwork(), moduleFile.getName());
                            } else {
                                dataAccess.deleteModuleFile(moduleFile.getServiceId(), moduleFile.getModuleId(), moduleFile.getNetwork(), moduleFile.getName());
                                logger.info("MF: {} {} {} BOOM!!", service.getServiceAbbr(), moduleFile.getNetwork(), moduleFile.getName());
                            }
                        }
                    }
                }
            }
        }
    }

    private DataAccessUpdater() {
    }
}
