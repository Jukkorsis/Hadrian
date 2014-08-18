package com.northernwall.hadrian;

import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceHeader;
import com.northernwall.hadrian.domain.Services;
import com.northernwall.hadrian.domain.VersionHeader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.lightcouch.CouchDbClient;
import org.lightcouch.NoDocumentException;
import org.lightcouch.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoaRepDataAccess {

    private final static Logger logger = LoggerFactory.getLogger(SoaRepDataAccess.class);

    private final CouchDbClient dbClient;

    public SoaRepDataAccess(CouchDbClient dbClient) {
        this.dbClient = dbClient;

        List<ServiceHeader> temp = dbClient.view("_all_docs").includeDocs(true).query(ServiceHeader.class);
        if (temp == null || temp.isEmpty()) {
            loadFromFile();
        }
    }

    private void loadFromFile() {
        FileReader file = null;
        String fileName = "data.json";
        Services services;
        try {
            file = new FileReader(fileName);
            services = dbClient.getGson().fromJson(file, Services.class);
            logger.info("Init complete, data file {} loaded", fileName);
        } catch (FileNotFoundException ex) {
            logger.error("Init failed, could not find data file {}", fileName);
            throw new RuntimeException(ex);
        } finally {
            try {
                file.close();
            } catch (IOException ex) {
                logger.error("Failed close data file {} after init load", fileName);
            }
        }

        for (Service service : services.services) {
            save(service);
        }
    }

    public List<ServiceHeader> getServiceHeaders() {
        return dbClient.view("_all_docs").includeDocs(true).query(ServiceHeader.class);
    }

    public List<Service> getServices() {
        return dbClient.view("_all_docs").includeDocs(true).query(Service.class);
    }

    public Service getService(String id) {
        try {
            return dbClient.find(Service.class, id);
        } catch (NoDocumentException nde) {
            return null;
        }
    }

    public void save(Service service) {
        Response rev = dbClient.save(service);
        logger.info("Save: id {} rev {} error {} reason {}", rev.getId(), rev.getRev(), rev.getError(), rev.getReason());
    }

    public void update(Service service) {
        Response rev = dbClient.update(service);
        logger.info("Update: id {} rev {} error {} reason {}", rev.getId(), rev.getRev(), rev.getError(), rev.getReason());
    }

    public List<VersionHeader> getVersions() {
        List<VersionResult> results = dbClient.view("app/versions").query(VersionResult.class);
        List<VersionHeader> versions = new LinkedList<>();
        for (VersionResult result : results) {
            versions.add(result.value);
        }
        return versions;
    }

    public class VersionResult {
        public VersionHeader value;
    }
}
