package com.northernwall.hadrian.db;

import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.domain.ConfigItem;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceHeader;
import com.northernwall.hadrian.domain.ServiceRefView;
import com.northernwall.hadrian.domain.VersionView;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;
import org.lightcouch.DesignDocument;
import org.lightcouch.NoDocumentException;
import org.lightcouch.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CouchDataAccess implements DataAccess {

    private final static Logger logger = LoggerFactory.getLogger(CouchDataAccess.class);

    private final int port;
    private final String host;
    private final CouchDbClient dbClient;
    private Config config;

    public CouchDataAccess(Properties properties) {
        port = Integer.parseInt(properties.getProperty("couchdb.port", "5984"));
        host = properties.getProperty("couchdb.host", "127.0.0.1");
        CouchDbProperties dbProperties = new CouchDbProperties()
                .setDbName(properties.getProperty("couchdb.name", "soarep"))
                .setCreateDbIfNotExist(Boolean.parseBoolean(properties.getProperty("couchdb.if-not-exist", "true")))
                .setProtocol(properties.getProperty("couchdb.protocol", "http"))
                .setHost(host)
                .setPort(port)
                .setMaxConnections(100)
                .setConnectionTimeout(0);
        dbClient = new CouchDbClient(dbProperties);
        logger.info("Couch access established");

        Map<String, DesignDocument.MapReduce> views = new HashMap<>();

        DesignDocument.MapReduce mapReduce = new DesignDocument.MapReduce();
        mapReduce.setMap("function(doc) {if (doc._id != \"_design/app\" && doc._id != \"SoaConfig\") {emit(doc._id, {_id: doc._id, name: doc.name, date: doc.date, team: doc.team, description: doc.description, state: doc.state, access: doc.access, type: doc.type, imageLogo: doc.imageLogo});}}");
        views.put("services", mapReduce);

        mapReduce = new DesignDocument.MapReduce();
        mapReduce.setMap("function(doc) {if (doc._id != \"_design/app\" && doc._id != \"SoaConfig\") {doc.versions.forEach(function(version) {var refs = version.uses.length + version.usedby.length; emit(null, {serviceId: doc._id, name: doc.name, team: doc.team, state: doc.state, access: doc.access, type: doc.type, versionId: version.api, status: version.status, refs: refs});});}}");
        views.put("versions", mapReduce);

        mapReduce = new DesignDocument.MapReduce();
        mapReduce.setMap("function(doc) {if (doc._id != \"_design/app\" && doc._id != \"SoaConfig\") {doc.versions.forEach(function(version) {version.uses.forEach(function(ref) {emit(null, {serviceId: doc._id, versionId: version.api, refServiceId: ref.service, refVersionId: ref.version, scope: ref.scope});});});}}");
        views.put("refs", mapReduce);

        DesignDocument designDoc = new DesignDocument();
        designDoc.setViews(views);
        designDoc.setId("_design/app");
        designDoc.setLanguage("javascript");
        dbClient.design().synchronizeWithDb(designDoc);
        logger.info("Couch views synced");
    }

    @Override
    public Config getConfig() {
        try {
            return dbClient.find(Config.class, "SoaConfig");
        } catch (NoDocumentException nde) {
            logger.info("No config found, returning empty config");
            Config temp = new Config();
            temp.setId("SoaConfig");
            return temp;
        }
    }

    @Override
    public void save(Config config) {
        if (config.getRevision() == null) {
            Response rev = dbClient.save(config);
            logger.info("Config Saved: rev {}", rev.getRev());
        } else {
            Response rev = dbClient.update(config);
            logger.info("Config Updated: rev {}", rev.getRev());
        }
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
        if (service.getRevision() == null) {
            Response rev = dbClient.save(service);
            logger.info("Service Saved: id {} rev {} error {} reason {}", rev.getId(), rev.getRev(), rev.getError(), rev.getReason());
        } else {
            Response rev = dbClient.update(service);
            logger.info("Service Updated: id {} rev {} error {} reason {}", rev.getId(), rev.getRev(), rev.getError(), rev.getReason());
        }
    }

    @Override
    public void uploadImage(String serviceId, String name, String contentType, InputStream openStream) {
        Service service = getService(serviceId);
        if (service == null) {
            logger.warn("Could not find service {} to add attachment {}", serviceId, name);
            return;
        }
        dbClient.saveAttachment(openStream, name, contentType, service.getId(), service.getRevision());
        service = getService(serviceId);
        if (service.isImageLogoBlank()) {
            service.imageLogo = "/services/" + serviceId + "/image/" + name;
            dbClient.update(service);
        }
        logger.info("Uploaded attachment {} of type {} to {} {}", name, contentType, service.getId(), service.getRevision());
    }

    @Override
    public InputStream downloadImage(String serviceId, String name) throws IOException {
        Service service = getService(serviceId);
        if (service == null) {
            logger.warn("Could not find service {} to download attachment {}", serviceId, name);
            return null;
        }
        if (service.getAttachments() != null) {
            for (String key : service.getAttachments().keySet()) {
                if (key.equals(name)) {
                    String url = "http://" + host + ":" + port + "/soarep/" + serviceId + "/" + name;
                    logger.info("Downloading attachement {}", url);
                    HttpGet get = new HttpGet(url);
                    HttpResponse response = dbClient.executeRequest(get);
                    return response.getEntity().getContent();
                }
            }
        }
        return null;
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
