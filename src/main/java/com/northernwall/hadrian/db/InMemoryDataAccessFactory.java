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

package com.northernwall.hadrian.db;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.Const;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryDataAccessFactory implements DataAccessFactory, Runnable {
    private final static Logger logger = LoggerFactory.getLogger(InMemoryDataAccessFactory.class);
    
    private InMemoryDataAccess dataAccess;
    private String dataFileName;

    @Override
    public DataAccess createDataAccess(Properties properties) {
        dataFileName = properties.getProperty(Const.IN_MEMORY_DATA_FILE_NAME, Const.IN_MEMORY_DATA_FILE_NAME_DEFAULT);
        dataAccess = load();
        
        if (dataAccess == null) {
            logger.info("Creating a new In Memory store");
            dataAccess = new InMemoryDataAccess();
        }
        
        Thread thread = new Thread(this);
        Runtime.getRuntime().addShutdownHook(thread);
        
        return dataAccess;
    }

    private InMemoryDataAccess load() {
        File file = new File(dataFileName);
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
    
    @Override
    public void run() {
        File file = new File(dataFileName);
        Gson gson = new Gson();
        try (JsonWriter jw = new JsonWriter(new FileWriter(file))) {
            gson.toJson(dataAccess, InMemoryDataAccess.class, jw);
            logger.info("In Memory store saved to disk, {}", file.getName());
        } catch (Exception ex) {
            logger.error("Faled to save In Memory, {}", ex.getMessage());
        }
    }

}