package com.northernwall.hadrian;

import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceHeader;
import com.northernwall.hadrian.domain.VersionHeader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.lightcouch.CouchDbClient;
import org.lightcouch.DesignDocument;
import org.lightcouch.NoDocumentException;
import org.lightcouch.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoaRepDataAccess {

    private final static Logger logger = LoggerFactory.getLogger(SoaRepDataAccess.class);

    private final CouchDbClient dbClient;

    public SoaRepDataAccess(CouchDbClient dbClient) {
        this.dbClient = dbClient;
        
        Map<String, DesignDocument.MapReduce> views = new HashMap<>();
        DesignDocument.MapReduce mapReduce = new DesignDocument.MapReduce();
        mapReduce.setMap("function(doc) {doc.versions.forEach(function(version) {emit(null, {serviceId: doc._id, versionId: version.api});});}");
        views.put("versions", mapReduce);
        DesignDocument designDoc = new DesignDocument();
        designDoc.setViews(views);
        designDoc.setId("_design/app");
        designDoc.setLanguage("javascript");
        dbClient.design().synchronizeWithDb(designDoc);
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
