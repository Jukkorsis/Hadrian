package com.northernwall.hadrian.db;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.domain.CustomFunction;
import com.northernwall.hadrian.domain.DataStore;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceRef;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.VipRef;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryDataAccessFactory implements DataAccessFactory, Runnable {
    private final static Logger logger = LoggerFactory.getLogger(InMemoryDataAccessFactory.class);
    private InMemoryDataAccess dataAccess;

    @Override
    public DataAccess createDataAccess(Properties properties) {
        dataAccess = load();
        
        if (dataAccess == null) {
            logger.info("Creating a new In Memory store");
            dataAccess = new InMemoryDataAccess();
        }
        
        init(dataAccess);
        
        Thread thread = new Thread(this);
        Runtime.getRuntime().addShutdownHook(thread);
        
        return dataAccess;
    }

    private InMemoryDataAccess load() {
        File file = new File("data.json");
        if (!file.exists()) {
            return null;
        }
        
        Gson gson = new Gson();
        try {  
            return gson.fromJson(new FileReader(file), InMemoryDataAccess.class);
        } catch (FileNotFoundException ex) {
            logger.error("Failed to read file, {}", ex.getMessage());
            return null;
        }
    }
    
    private void init(DataAccess dataAccess) {
        List<Team> teams = dataAccess.getTeams();
        if (teams == null || teams.isEmpty()) {
            logger.info("Creating the example data");
            
            Team team = new Team("My Team");
            dataAccess.saveTeam(team);
        }
    }

    @Override
    public void run() {
        File file = new File("data.json");
        Gson gson = new Gson();
        try (JsonWriter jw = new JsonWriter(new FileWriter(file))) {
            gson.toJson(dataAccess, InMemoryDataAccess.class, jw);
            logger.info("In Memory store saved to disk, {}", file.getName());
        } catch (Exception ex) {
            logger.error("Faled to save In Memory, {}", ex.getMessage());
        }
    }

}