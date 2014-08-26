package com.northernwall.hadrian.db;

import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceHeader;
import com.northernwall.hadrian.domain.ServiceRefView;
import com.northernwall.hadrian.domain.VersionView;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;
import org.lightcouch.DesignDocument;
import org.lightcouch.NoDocumentException;
import org.lightcouch.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CouchDataAccess implements DataAccess {

    private final static Logger logger = LoggerFactory.getLogger(CouchDataAccess.class);

    private final CouchDbClient dbClient;

    public CouchDataAccess(Properties properties) {
        CouchDbProperties dbProperties = new CouchDbProperties()
                .setDbName(properties.getProperty("couchdb.name","soarep"))
                .setCreateDbIfNotExist(Boolean.parseBoolean(properties.getProperty("couchdb.if-not-exist", "true")))
                .setProtocol(properties.getProperty("couchdb.protocol", "http"))
                .setHost(properties.getProperty("couchdb.host", "127.0.0.1"))
                .setPort(Integer.parseInt(properties.getProperty("couchdb.port","5984")))
                .setMaxConnections(100)
                .setConnectionTimeout(0);
        dbClient = new CouchDbClient(dbProperties);
        logger.info("Couch access established");
        
        Map<String, DesignDocument.MapReduce> views = new HashMap<>();

        DesignDocument.MapReduce mapReduce = new DesignDocument.MapReduce();
        mapReduce.setMap("function(doc) {if (doc._id != \"_design/app\") {emit(doc._id, {_id: doc._id, name: doc.name, date: doc.date, team: doc.team, description: doc.description, access: doc.access, type: doc.type, imageLogo: doc.imageLogo});}}");
        views.put("services", mapReduce);
        
        mapReduce = new DesignDocument.MapReduce();
        mapReduce.setMap("function(doc) {if (doc._id != \"_design/app\") {doc.versions.forEach(function(version) {emit(null, {serviceId: doc._id, name: doc.name, type: doc.team, type: doc.team, access: doc.access, versionId: version.api, status: version.status});});}}");
        views.put("versions", mapReduce);
        
        mapReduce = new DesignDocument.MapReduce();
        mapReduce.setMap("function(doc) {if (doc._id != \"_design/app\") {doc.versions.forEach(function(version) {version.uses.forEach(function(ref) {emit(null, {serviceId: doc._id, versionId: version.api, refServiceId: ref.service, refVersionId: ref.version, scope: ref.scope});});});}}");
        views.put("refs", mapReduce);
        
        DesignDocument designDoc = new DesignDocument();
        designDoc.setViews(views);
        designDoc.setId("_design/app");
        designDoc.setLanguage("javascript");
        dbClient.design().synchronizeWithDb(designDoc);
        logger.info("Couch views synced");
    }
    
    @Override
    public List<ServiceHeader> getServiceHeaders() {
        return dbClient.view("app/services").includeDocs(true).query(ServiceHeader.class);
    }

    @Override
    public Service getService(String id) {
        try {
            return dbClient.find(Service.class, id);
        } catch (NoDocumentException nde) {
            return null;
        }
    }

    @Override
    public void save(Service service) {
        Response rev = dbClient.save(service);
        logger.info("Save: id {} rev {} error {} reason {}", rev.getId(), rev.getRev(), rev.getError(), rev.getReason());
    }

    @Override
    public void update(Service service) {
        Response rev = dbClient.update(service);
        logger.info("Update: id {} rev {} error {} reason {}", rev.getId(), rev.getRev(), rev.getError(), rev.getReason());
    }

    @Override
    public List<VersionView> getVersionVeiw() {
        List<VersionViewResult> results = dbClient.view("app/versions").query(VersionViewResult.class);
        List<VersionView> versions = new LinkedList<>();
        for (VersionViewResult result : results) {
            versions.add(result.value);
        }
        return versions;
    }

    public class VersionViewResult {
        public VersionView value;
    }
    
    @Override
    public List<ServiceRefView> getServiceRefVeiw() {
        List<ServiceRefViewResult> results = dbClient.view("app/refs").query(ServiceRefViewResult.class);
        List<ServiceRefView> refs = new LinkedList<>();
        for (ServiceRefViewResult result : results) {
            refs.add(result.value);
        }
        return refs;
    }

    public class ServiceRefViewResult {
        public ServiceRefView value;
    }
    
}
