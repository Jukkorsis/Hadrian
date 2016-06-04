package com.northernwall.hadrian.db;

import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleType;
import com.northernwall.hadrian.domain.Service;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAccessUpdater {

    private final static Logger logger = LoggerFactory.getLogger(DataAccessUpdater.class);

    public static void update(DataAccess dataAccess) {
        String version = dataAccess.getVersion();

        if (version == null) {
            dataAccess.setVersion("1.5");
            update(dataAccess);
        }
        
        logger.info("Current DB version is {}, no upgrade required.", version);
    }

    private DataAccessUpdater() {
    }
}
