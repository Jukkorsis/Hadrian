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
package com.northernwall.hadrian.db.cassandra;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Session.State;
import com.google.gson.Gson;
import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.StringUtils;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.db.SearchResult;
import com.northernwall.hadrian.db.SearchSpace;
import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.CustomFunction;
import com.northernwall.hadrian.domain.DataStore;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleFile;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ModuleRef;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.handlers.utility.HealthWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.dshops.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraDataAccess implements DataAccess {

    private final static Logger LOGGER = LoggerFactory.getLogger(CassandraDataAccess.class);

    private static final String CQL_SELECT_PRE = "SELECT * FROM ";
    private static final String CQL_SELECT_POST = ";";

    private final String username;
    private final String dataCenter;
    private final Session session;

    private final PreparedStatement auditSelect;
    private final PreparedStatement auditInsert;
    private final PreparedStatement auditOutputSelect;
    private final PreparedStatement auditOutputInsert;
    private final PreparedStatement versionSelect;
    private final PreparedStatement versionInsert;
    private final PreparedStatement versionUpdate;
    private final PreparedStatement customFunctionSelect;
    private final PreparedStatement customFunctionSelect2;
    private final PreparedStatement customFunctionInsert;
    private final PreparedStatement customFunctionUpdate;
    private final PreparedStatement customFunctionDelete;
    private final PreparedStatement dataStoreSelect;
    private final PreparedStatement dataStoreSelect2;
    private final PreparedStatement dataStoreInsert;
    private final PreparedStatement dataStoreUpdate;
    private final PreparedStatement dataStoreDelete;
    private final PreparedStatement hostSelect;
    private final PreparedStatement hostSelect2;
    private final PreparedStatement hostInsert;
    private final PreparedStatement hostUpdate;
    private final PreparedStatement hostDelete;
    private final PreparedStatement searchSelect;
    private final PreparedStatement searchSelect2;
    private final PreparedStatement searchInsert;
    private final PreparedStatement searchDelete;
    private final PreparedStatement moduleSelect;
    private final PreparedStatement moduleSelect2;
    private final PreparedStatement moduleInsert;
    private final PreparedStatement moduleUpdate;
    private final PreparedStatement moduleDelete;
    private final PreparedStatement moduleFileSelect;
    private final PreparedStatement moduleFileSelect2;
    private final PreparedStatement moduleFileInsert;
    private final PreparedStatement moduleFileUpdate;
    private final PreparedStatement moduleFileDelete;
    private final PreparedStatement serviceSelect;
    private final PreparedStatement serviceInsert;
    private final PreparedStatement serviceUpdate;
    private final PreparedStatement moduleRefSelectClient;
    private final PreparedStatement moduleRefSelectServer;
    private final PreparedStatement moduleRefInsertClient;
    private final PreparedStatement moduleRefInsertServer;
    private final PreparedStatement moduleRefDeleteClient;
    private final PreparedStatement moduleRefDeleteServer;
    private final PreparedStatement teamSelect;
    private final PreparedStatement teamInsert;
    private final PreparedStatement teamUpdate;
    private final PreparedStatement vipSelect;
    private final PreparedStatement vipSelect2;
    private final PreparedStatement vipInsert;
    private final PreparedStatement vipUpdate;
    private final PreparedStatement vipDelete;
    private final PreparedStatement workItemSelect;
    private final PreparedStatement workItemInsert;
    private final PreparedStatement workItemUpdate;
    private final PreparedStatement workItemDelete;
    private final PreparedStatement workItemStatusSelect;
    private final PreparedStatement workItemStatusInsert;
    private final PreparedStatement statusSelect;
    private final PreparedStatement statusInsert;

    private final Gson gson;

    public CassandraDataAccess(Cluster cluster, String keyspace, String username, String dataCenter, int auditTimeToLive, int statusTimeToLive, Gson gson, MetricRegistry metricRegistry) {
        this.username = username;
        this.dataCenter = dataCenter;
        session = cluster.connect(keyspace);

        LOGGER.info("Praparing version statements...");
        versionSelect = session.prepare("SELECT * FROM version WHERE component = ?;");
        versionSelect.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        versionInsert = session.prepare("INSERT INTO version (component, version) VALUES (?, ?);");
        versionInsert.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        versionUpdate = session.prepare("UPDATE version SET version = ? WHERE component = ? ;");
        versionUpdate.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        LOGGER.info("Praparing customFunction statements...");
        customFunctionSelect = session.prepare("SELECT * FROM customFunction WHERE serviceId = ?;");
        customFunctionSelect.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        customFunctionSelect2 = session.prepare("SELECT * FROM customFunction WHERE serviceId = ? AND id = ?;");
        customFunctionSelect2.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        customFunctionInsert = session.prepare("INSERT INTO customFunction (serviceId, id, data) VALUES (?, ?, ?);");
        customFunctionInsert.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        customFunctionUpdate = session.prepare("UPDATE customFunction SET data = ? WHERE serviceId = ? AND id = ?;");
        customFunctionUpdate.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        customFunctionDelete = session.prepare("DELETE FROM customFunction WHERE serviceId = ? AND id = ?;");
        customFunctionDelete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        LOGGER.info("Praparing dataStore statements...");
        dataStoreSelect = session.prepare("SELECT * FROM dataStore WHERE serviceId = ?;");
        dataStoreSelect.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        dataStoreSelect2 = session.prepare("SELECT * FROM dataStore WHERE serviceId = ? AND id = ?;");
        dataStoreSelect2.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        dataStoreInsert = session.prepare("INSERT INTO dataStore (serviceId, id, data) VALUES (?, ?, ?);");
        dataStoreInsert.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        dataStoreUpdate = session.prepare("UPDATE dataStore SET data = ? WHERE serviceId = ? AND id = ?;");
        dataStoreUpdate.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        dataStoreDelete = session.prepare("DELETE FROM dataStore WHERE serviceId = ? AND id = ?;");
        dataStoreDelete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        LOGGER.info("Praparing host statements...");
        hostSelect = session.prepare("SELECT * FROM host WHERE serviceId = ?;");
        hostSelect.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        hostSelect2 = session.prepare("SELECT * FROM host WHERE serviceId = ? AND id = ?;");
        hostSelect2.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        hostInsert = session.prepare("INSERT INTO host (serviceId, id, data) VALUES (?, ?, ?);");
        hostInsert.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        hostUpdate = session.prepare("UPDATE host SET data = ? WHERE serviceId = ? AND id = ?;");
        hostUpdate.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        hostDelete = session.prepare("DELETE FROM host WHERE serviceId = ? AND id = ?;");
        hostDelete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        LOGGER.info("Praparing module statements...");
        moduleSelect = session.prepare("SELECT * FROM module WHERE serviceId = ?;");
        moduleSelect.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        moduleSelect2 = session.prepare("SELECT * FROM module WHERE serviceId = ? AND id = ?;");
        moduleSelect2.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        moduleInsert = session.prepare("INSERT INTO module (serviceId, id, data) VALUES (?, ?, ?);");
        moduleInsert.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        moduleUpdate = session.prepare("UPDATE module SET data = ? WHERE serviceId = ? AND id = ?;");
        moduleUpdate.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        moduleDelete = session.prepare("DELETE FROM module WHERE serviceId = ? AND id = ?;");
        moduleDelete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        LOGGER.info("Praparing moduleFile statements...");
        moduleFileSelect = session.prepare("SELECT * FROM moduleFile WHERE serviceId = ? AND moduleId = ? AND network = ?;");
        moduleFileSelect.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        moduleFileSelect2 = session.prepare("SELECT * FROM moduleFile WHERE serviceId = ? AND moduleId = ? AND network = ? AND name = ?;");
        moduleFileSelect2.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        moduleFileInsert = session.prepare("INSERT INTO moduleFile (serviceId, moduleId, network, name, data) VALUES (?, ?, ?, ?, ?);");
        moduleFileInsert.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        moduleFileUpdate = session.prepare("UPDATE moduleFile SET data = ? WHERE serviceId = ? AND moduleId = ? AND network = ? AND name = ?;");
        moduleFileUpdate.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        moduleFileDelete = session.prepare("DELETE FROM moduleFile WHERE serviceId = ? AND moduleId = ? AND network = ? AND name = ?;");
        moduleFileDelete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        LOGGER.info("Praparing service statements...");
        serviceSelect = session.prepare("SELECT * FROM service WHERE id = ?;");
        serviceSelect.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        serviceInsert = session.prepare("INSERT INTO service (id, data) VALUES (?, ?);");
        serviceInsert.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        serviceUpdate = session.prepare("UPDATE service SET data = ? WHERE id = ?;");
        serviceUpdate.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        LOGGER.info("Praparing moduleRef statements...");
        moduleRefSelectClient = session.prepare("SELECT * FROM moduleRefClient WHERE clientServiceId = ? AND clientModuleId = ?;");
        moduleRefSelectClient.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        moduleRefSelectServer = session.prepare("SELECT * FROM moduleRefServer WHERE serverServiceId = ? AND serverModuleId = ?;");
        moduleRefSelectServer.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        moduleRefInsertClient = session.prepare("INSERT INTO moduleRefClient (clientServiceId, clientModuleId, serverServiceId, serverModuleId) VALUES (?, ?, ?, ?);");
        moduleRefInsertClient.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        moduleRefInsertServer = session.prepare("INSERT INTO moduleRefServer (serverServiceId, serverModuleId, clientServiceId, clientModuleId) VALUES (?, ?, ?, ?);");
        moduleRefInsertServer.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        moduleRefDeleteClient = session.prepare("DELETE FROM moduleRefClient WHERE clientServiceId = ? AND clientModuleId = ? AND serverServiceId = ? AND serverModuleId = ?;");
        moduleRefDeleteClient.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        moduleRefDeleteServer = session.prepare("DELETE FROM moduleRefServer WHERE serverServiceId = ? AND serverModuleId = ? AND clientServiceId = ? AND clientModuleId = ?;");
        moduleRefDeleteServer.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        LOGGER.info("Praparing team statements...");
        teamSelect = session.prepare("SELECT * FROM team WHERE id = ?;");
        teamSelect.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        teamInsert = session.prepare("INSERT INTO team (id, data) VALUES (?, ?);");
        teamInsert.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        teamUpdate = session.prepare("UPDATE team SET data = ? WHERE id = ?;");
        teamUpdate.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        LOGGER.info("Praparing vip statements...");
        vipSelect = session.prepare("SELECT * FROM vip WHERE serviceId = ?;");
        vipSelect.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        vipSelect2 = session.prepare("SELECT * FROM vip WHERE serviceId = ? AND id = ?;");
        vipSelect2.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        vipInsert = session.prepare("INSERT INTO vip (serviceId, id, data) VALUES (?, ?, ?);");
        vipInsert.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        vipUpdate = session.prepare("UPDATE vip SET data = ? WHERE serviceId = ? AND id = ?;");
        vipUpdate.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        vipDelete = session.prepare("DELETE FROM vip WHERE serviceId = ? AND id = ?;");
        vipDelete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        LOGGER.info("Praparing workItem statements...");
        workItemSelect = session.prepare("SELECT * FROM workItem WHERE id = ?;");
        workItemSelect.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        workItemInsert = session.prepare("INSERT INTO workItem (id, data) VALUES (?, ?);");
        workItemInsert.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        workItemUpdate = session.prepare("UPDATE workItem SET data = ? WHERE id = ?;");
        workItemUpdate.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        workItemDelete = session.prepare("DELETE FROM workItem WHERE id = ?;");
        workItemDelete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        LOGGER.info("Praparing workItemStatus statements...");
        workItemStatusSelect = session.prepare("SELECT * FROM workItemStatus WHERE id = ?;");
        workItemStatusSelect.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        workItemStatusInsert = session.prepare("INSERT INTO workItemStatus (id, status) VALUES (?, ?) USING TTL 86400;");
        workItemStatusInsert.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        LOGGER.info("Praparing audit statements...");
        LOGGER.info("Audit TTL {}", auditTimeToLive);
        auditSelect = session.prepare("SELECT data FROM auditRecord WHERE serviceId = ? AND year = ? AND month = ? AND day = ?");
        auditInsert = session.prepare("INSERT INTO auditRecord (serviceId, year, month, day, time, data) VALUES (?, ?, ?, ?, now(), ?) USING TTL " + auditTimeToLive + ";");
        auditInsert.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        auditOutputSelect = session.prepare("SELECT data FROM auditOutput WHERE serviceId = ? AND auditId = ?");
        auditOutputInsert = session.prepare("INSERT INTO auditOutput (serviceId, auditId, data) VALUES (?, ?, ?) USING TTL " + auditTimeToLive + ";");
        auditOutputInsert.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        LOGGER.info("Praparing search statements...");
        searchSelect = session.prepare("SELECT * FROM searchName WHERE searchSpace = ? AND searchText1 = ? AND searchText2 = ?;");
        searchSelect.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        searchSelect2 = session.prepare("SELECT * FROM searchName WHERE searchSpace = ? AND searchText1 = ?;");
        searchSelect2.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        searchInsert = session.prepare("INSERT INTO searchName (searchSpace, searchText1, searchText2, teamId, serviceId, moduleId, hostId, vipId) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
        searchInsert.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        searchDelete = session.prepare("DELETE FROM searchName WHERE searchSpace = ? AND searchText1 = ? AND searchText2 = ?;");
        searchDelete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        LOGGER.info("Praparing status statements...");
        LOGGER.info("Status TTL {}", statusTimeToLive);
        statusSelect = session.prepare("SELECT id, dateOf(time), busy, status, statusCode FROM entityStatus WHERE id = ? ORDER BY time DESC LIMIT 1;");
        statusInsert = session.prepare("INSERT INTO entityStatus (id, time, busy, status, statusCode) VALUES (?, now(), ?, ?, ?) USING TTL " + statusTimeToLive + ";");

        LOGGER.info("Prapared statements created");

        this.gson = gson;
    }

    @Override
    public boolean getAvailability() {
        return true;
    }

    @Override
    public void getHealth(HealthWriter writer) throws IOException {
        Metadata metadata = session.getCluster().getMetadata();
        State state = session.getState();
        writer.addStringLine("Cassandra - Schema Version", getVersion());
        writer.addStringLine("Cassandra - Username", username);
        writer.addStringLine("Cassandra - Cluster", metadata.getClusterName());
        if (dataCenter != null) {
            writer.addStringLine("Cassandra - Preferred DC", dataCenter);
        }
        writer.addStringLine("Cassandra - Keyspace", session.getLoggedKeyspace());
        List<String> dcs = new LinkedList<>();
        for (com.datastax.driver.core.Host host : metadata.getAllHosts()) {
            if (!dcs.contains(host.getDatacenter())) {
                dcs.add(host.getDatacenter());
            }
        }
        for (String dc : dcs) {
            for (com.datastax.driver.core.Host host : metadata.getAllHosts()) {
                if (dc.equals(host.getDatacenter())) {
                    writer.addStringLine(
                            "Cassandra - " + host.getDatacenter(),
                            host.getAddress().getHostAddress()
                            + "  "
                            + host.getRack()
                            + "  "
                            + host.getState()
                            + "  v"
                            + host.getCassandraVersion().toString()
                            + "  open="
                            + state.getOpenConnections(host)
                            + "  trashed="
                            + state.getTrashedConnections(host));
                }
            }
        }
    }

    @Override
    public String getVersion() {
        BoundStatement boundStatement = new BoundStatement(versionSelect);
        ResultSet results = session.execute(boundStatement.bind("datastore"));
        for (Row row : results) {
            return row.getString("version");
        }
        return null;
    }

    @Override
    public void setVersion(String version) {
        if (getVersion() == null) {
            BoundStatement boundStatement = new BoundStatement(versionInsert);
            session.execute(boundStatement.bind("datastore", version));
        } else {
            BoundStatement boundStatement = new BoundStatement(versionUpdate);
            session.execute(boundStatement.bind(version, "datastore"));
        }
    }

    @Override
    public List<Team> getTeams() {
        return getData("team", Team.class);
    }

    @Override
    public Team getTeam(String teamId) {
        return getData(teamId, teamSelect, Team.class);
    }

    @Override
    public void saveTeam(Team team) {
        saveData(team.getTeamId(), gson.toJson(team), teamInsert);
    }

    @Override
    public void updateTeam(Team team) {
        updateData(team.getTeamId(), gson.toJson(team), teamUpdate);
    }

    @Override
    public List<Service> getAllServices() {
        List<Service> services = getData("service", Service.class);
        Collections.sort(services);
        return services;
    }

    @Override
    public List<Service> getActiveServices() {
        List<Service> services = getData("service", Service.class);
        List<Service> temp = new LinkedList<>();
        for (Service service : services) {
            if (service.isActive()) {
                temp.add(service);
            }
        }
        Collections.sort(temp);
        return temp;
    }

    @Override
    public Service getService(String serviceId) {
        return getData(serviceId, serviceSelect, Service.class);
    }

    @Override
    public void saveService(Service service) {
        saveData(service.getServiceId(), gson.toJson(service), serviceInsert);
    }

    @Override
    public void updateService(Service service) {
        updateData(service.getServiceId(), gson.toJson(service), serviceUpdate);
    }

    @Override
    public List<Host> getHosts(String serviceId) {
        List<Host> hosts = getServiceData(serviceId, hostSelect, Host.class);
        if (hosts == null || hosts.isEmpty()) {
            return hosts;
        }
        for (Host host : hosts) {
            getHostStatus(host);
        }
        Collections.sort(hosts);
        return hosts;
    }

    @Override
    public Host getHost(String serviceId, String hostId) {
        Host host = getServiceData(serviceId, hostId, hostSelect2, Host.class);
        if (host == null) {
            return null;
        }
        getHostStatus(host);
        return host;
    }

    private void getHostStatus(Host host) {
        Row row = getStatus(host.getHostId());
        if (row != null) {
            String status = row.getString("status");
            if (status == null || status.isEmpty()) {
                status = Const.STATUS_NO;
            } else if (status.contains("%%")) {
                Date date = row.getTimestamp(1);
                status = status.replace("%%", StringUtils.dateRange(date.getTime()));
            }

            String statusCode = row.getString("statusCode");
            if (statusCode == null || statusCode.isEmpty()) {
                statusCode = Const.STATUS_ERROR;
            }

            host.setStatus(row.getBool("busy"), status, statusCode);
        } else {
            host.setStatus(false, Const.STATUS_NO, Const.STATUS_NO);
        }
    }

    @Override
    public void saveHost(Host host) {
        saveServiceData(host.getServiceId(), host.getHostId(), gson.toJson(host), hostInsert);
    }

    @Override
    public void updateHost(Host host) {
        updateServiceData(host.getServiceId(), host.getHostId(), gson.toJson(host), hostUpdate);
    }

    @Override
    public void deleteHost(Host host) {
        deleteServiceData(host.getServiceId(), host.getHostId(), hostDelete);
    }

    @Override
    public List<Module> getModules(String serviceId) {
        return getServiceData(serviceId, moduleSelect, Module.class);
    }

    @Override
    public Module getModule(String serviceId, String moduleId) {
        return getServiceData(serviceId, moduleId, moduleSelect2, Module.class);
    }

    @Override
    public void saveModule(Module module) {
        saveServiceData(module.getServiceId(), module.getModuleId(), gson.toJson(module), moduleInsert);
    }

    @Override
    public void updateModule(Module module) {
        updateServiceData(module.getServiceId(), module.getModuleId(), gson.toJson(module), moduleUpdate);
    }

    @Override
    public void deleteModule(String serviceId, String moduleId) {
        deleteServiceData(serviceId, moduleId, moduleDelete);
    }

    @Override
    public List<ModuleFile> getModuleFiles(String serviceId, String moduleId, String environment) {
        BoundStatement boundStatement = new BoundStatement(moduleFileSelect);
        ResultSet results = session.execute(boundStatement.bind(
                serviceId,
                moduleId,
                environment));
        if (results == null || results.isExhausted()) {
            return null;
        }
        List<ModuleFile> moduleFiles = new LinkedList<>();
        for (Row row : results) {
            moduleFiles.add(new ModuleFile(
                    serviceId,
                    moduleId,
                    environment,
                    row.getString("name"),
                    row.getString("data")));
        }

        return moduleFiles;
    }

    @Override
    public ModuleFile getModuleFile(String serviceId, String moduleId, String environment, String name) {
        BoundStatement boundStatement = new BoundStatement(moduleFileSelect2);
        ResultSet results = session.execute(boundStatement.bind(
                serviceId,
                moduleId,
                environment,
                name));
        Row row = results.one();
        if (row == null) {
            return null;
        }
        return new ModuleFile(
                serviceId,
                moduleId,
                environment,
                name,
                row.getString("data"));
    }

    @Override
    public void saveModuleFile(ModuleFile moduleFile) {
        BoundStatement boundStatement = new BoundStatement(moduleFileInsert);
        session.execute(boundStatement.bind(
                moduleFile.getServiceId(),
                moduleFile.getModuleId(),
                moduleFile.getEnvironment(),
                moduleFile.getName(),
                moduleFile.getContents()));
    }

    @Override
    public void updateModuleFile(ModuleFile moduleFile) {
        BoundStatement boundStatement = new BoundStatement(moduleFileUpdate);
        session.execute(boundStatement.bind(
                moduleFile.getContents(),
                moduleFile.getServiceId(),
                moduleFile.getModuleId(),
                moduleFile.getEnvironment(),
                moduleFile.getName()));
    }

    @Override
    public void deleteModuleFile(String serviceId, String moduleId, String environment, String name) {
        BoundStatement boundStatement = new BoundStatement(moduleFileDelete);
        session.execute(boundStatement.bind(serviceId, moduleId, environment, name));
    }

    @Override
    public List<Vip> getVips(String serviceId) {
        List<Vip> vips = getServiceData(serviceId, vipSelect, Vip.class);
        if (vips == null || vips.isEmpty()) {
            return vips;
        }
        for (Vip vip : vips) {
            getVipStatus(vip);
        }
        Collections.sort(vips);
        return vips;
    }

    @Override
    public Vip getVip(String serviceId, String vipId) {
        Vip vip = getServiceData(serviceId, vipId, vipSelect2, Vip.class);
        if (vip == null) {
            return null;
        }
        getVipStatus(vip);
        return vip;
    }

    private void getVipStatus(Vip vip) {
        Row row = getStatus(vip.getVipId());
        if (row != null) {
            String status = row.getString("status");
            if (status == null || status.isEmpty()) {
                status = Const.STATUS_NO;
            } else if (status.contains("%%")) {
                Date date = row.getTimestamp(1);
                status = status.replace("%%", StringUtils.dateRange(date.getTime()));
            }

            String statusCode = row.getString("statusCode");
            if (statusCode == null || statusCode.isEmpty()) {
                statusCode = Const.STATUS_ERROR;
            }

            vip.setStatus(row.getBool("busy"), status, statusCode);
        } else {
            vip.setStatus(false, Const.STATUS_NO, Const.STATUS_NO);
        }
    }

    @Override
    public void saveVip(Vip vip) {
        saveServiceData(vip.getServiceId(), vip.getVipId(), gson.toJson(vip), vipInsert);
    }

    @Override
    public void updateVip(Vip vip) {
        updateServiceData(vip.getServiceId(), vip.getVipId(), gson.toJson(vip), vipUpdate);
    }

    @Override
    public void deleteVip(String serviceId, String vipId) {
        deleteServiceData(serviceId, vipId, vipDelete);
    }

    @Override
    public List<ModuleRef> getModuleRefs() {
        ResultSet results = session.execute(CQL_SELECT_PRE + "moduleRefClient" + CQL_SELECT_POST);
        List<ModuleRef> serviceRefs = new LinkedList<>();
        for (Row row : results) {
            serviceRefs.add(new ModuleRef(
                    row.getString("clientServiceId"),
                    row.getString("clientModuleId"),
                    row.getString("serverServiceId"),
                    row.getString("serverModuleId")));
        }
        return serviceRefs;
    }

    @Override
    public List<ModuleRef> getModuleRefsByClient(String clientServiceId, String clientModuleId) {
        BoundStatement boundStatement = new BoundStatement(moduleRefSelectClient);
        ResultSet results = session.execute(boundStatement.bind(clientServiceId, clientModuleId));
        List<ModuleRef> serviceRefs = new LinkedList<>();
        for (Row row : results) {
            serviceRefs.add(new ModuleRef(
                    row.getString("clientServiceId"),
                    row.getString("clientModuleId"),
                    row.getString("serverServiceId"),
                    row.getString("serverModuleId")));
        }
        return serviceRefs;
    }

    @Override
    public List<ModuleRef> getModuleRefsByServer(String serverServiceId, String serverModuleId) {
        BoundStatement boundStatement = new BoundStatement(moduleRefSelectServer);
        ResultSet results = session.execute(boundStatement.bind(serverServiceId, serverModuleId));
        List<ModuleRef> serviceRefs = new LinkedList<>();
        for (Row row : results) {
            serviceRefs.add(new ModuleRef(
                    row.getString("clientServiceId"),
                    row.getString("clientModuleId"),
                    row.getString("serverServiceId"),
                    row.getString("serverModuleId")));
        }
        return serviceRefs;
    }

    @Override
    public void saveModuleRef(ModuleRef moduleRef) {
        BoundStatement boundStatement;

        boundStatement = new BoundStatement(moduleRefInsertClient);
        session.execute(boundStatement.bind(
                moduleRef.getClientServiceId(),
                moduleRef.getClientModuleId(),
                moduleRef.getServerServiceId(),
                moduleRef.getServerModuleId()));

        boundStatement = new BoundStatement(moduleRefInsertServer);
        session.execute(boundStatement.bind(
                moduleRef.getServerServiceId(),
                moduleRef.getServerModuleId(),
                moduleRef.getClientServiceId(),
                moduleRef.getClientModuleId()));
    }

    @Override
    public void deleteModuleRef(String clientServiceId, String clientModuleId, String serverServiceId, String serverModuleId) {
        BoundStatement boundStatement;

        boundStatement = new BoundStatement(moduleRefDeleteClient);
        session.execute(boundStatement.bind(
                clientServiceId,
                clientModuleId,
                serverServiceId,
                serverModuleId));

        boundStatement = new BoundStatement(moduleRefDeleteServer);
        session.execute(boundStatement.bind(
                serverServiceId,
                serverModuleId,
                clientServiceId,
                clientModuleId));
    }

    @Override
    public List<CustomFunction> getCustomFunctions(String serviceId) {
        return getServiceData(serviceId, customFunctionSelect, CustomFunction.class);
    }

    @Override
    public CustomFunction getCustomFunction(String serviceId, String customFunctionId) {
        return getServiceData(serviceId, customFunctionId, customFunctionSelect2, CustomFunction.class);
    }

    @Override
    public void saveCustomFunction(CustomFunction customFunction) {
        saveServiceData(customFunction.getServiceId(), customFunction.getCustomFunctionId(), gson.toJson(customFunction), customFunctionInsert);
    }

    @Override
    public void updateCustomFunction(CustomFunction customFunction) {
        updateServiceData(customFunction.getServiceId(), customFunction.getCustomFunctionId(), gson.toJson(customFunction), customFunctionUpdate);
    }

    @Override
    public void deleteCustomFunction(String serviceId, String customFunctionId) {
        deleteServiceData(serviceId, customFunctionId, customFunctionDelete);
    }

    @Override
    public List<DataStore> getDataStores(String serviceId) {
        return getServiceData(serviceId, dataStoreSelect, DataStore.class);
    }

    @Override
    public DataStore getDataStore(String serviceId, String dataStoreId) {
        return getServiceData(serviceId, dataStoreId, dataStoreSelect2, DataStore.class);
    }

    @Override
    public void saveDataStore(DataStore dataStore) {
        saveServiceData(dataStore.getServiceId(), dataStore.getDataStoreId(), gson.toJson(dataStore), dataStoreInsert);
    }

    @Override
    public void updateDataStore(DataStore dataStore) {
        updateServiceData(dataStore.getServiceId(), dataStore.getDataStoreId(), gson.toJson(dataStore), dataStoreUpdate);
    }

    @Override
    public void deleteDataStore(String serviceId, String dataStoreId) {
        deleteServiceData(serviceId, dataStoreId, dataStoreDelete);
    }

    @Override
    public List<WorkItem> getWorkItems() {
        return getData("workItem", WorkItem.class);
    }

    @Override
    public WorkItem getWorkItem(String id) {
        return getData(id, workItemSelect, WorkItem.class);
    }

    @Override
    public void saveWorkItem(WorkItem workItem) {
        saveData(workItem.getId(), gson.toJson(workItem), workItemInsert);
    }

    @Override
    public void updateWorkItem(WorkItem workItem) {
        updateData(workItem.getId(), gson.toJson(workItem), workItemUpdate);
    }

    @Override
    public void deleteWorkItem(String id) {
        deleteData(id, workItemDelete);
    }

    @Override
    public int getWorkItemStatus(String id) {
        BoundStatement boundStatement = new BoundStatement(workItemStatusSelect);
        ResultSet results = session.execute(boundStatement.bind(id));
        Row row = results.one();
        if (row == null) {
            return -1;
        }
        return row.getInt("status");
    }

    @Override
    public void saveWorkItemStatus(String id, int status) {
        BoundStatement boundStatement = new BoundStatement(workItemStatusInsert);
        session.execute(boundStatement.bind(id, status));
    }

    @Override
    public void saveAudit(Audit audit, String output) {
        audit.auditId = UUID.randomUUID().toString();
        Calendar c = new GregorianCalendar();
        c.setTime(audit.getTimePerformed());
        BoundStatement boundStatement = new BoundStatement(auditInsert);
        session.execute(boundStatement.bind(
                audit.serviceId,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH),
                gson.toJson(audit)));

        if (output == null) {
            return;
        }
        output = output.trim();
        if (output.isEmpty()) {
            return;
        }
        boundStatement = new BoundStatement(auditOutputInsert);
        session.execute(boundStatement.bind(audit.serviceId, audit.auditId, output));
    }

    @Override
    public List<Audit> getAudit(String serviceId, int year, int month, int startDay, int endDay) {
        List<Audit> audits = new LinkedList<>();
        if (startDay < 1) {
            startDay = 1;
        }
        if (endDay < startDay) {
            endDay = startDay;
        }
        if (endDay > 31) {
            endDay = 31;
        }
        int day = startDay;
        while (day <= endDay) {
            BoundStatement boundStatement = new BoundStatement(auditSelect);
            ResultSet results = session.execute(boundStatement.bind(
                    serviceId,
                    year,
                    month,
                    day));
            for (Row row : results) {
                String data = row.getString("data");
                Audit audit = gson.fromJson(data, Audit.class);
                if (audit.auditId == null) {
                    audit.auditId = UUID.randomUUID().toString();
                }
                audits.add(audit);
            }
            day++;
        }
        return audits;
    }

    @Override
    public String getAuditOutput(String serviceId, String auditId) {
        BoundStatement boundStatement = new BoundStatement(auditOutputSelect);
        ResultSet results = session.execute(boundStatement.bind(serviceId, auditId));
        for (Row row : results) {
            return row.getString("data");
        }
        return null;
    }

    @Override
    public SearchResult doSearch(SearchSpace searchSpace, String searchText1) {
        return doSearch(searchSpace, searchText1, "-");
    }

    private SearchResult doSearch(SearchSpace searchSpace, String searchText1, String searchText2) {
        BoundStatement boundStatement = new BoundStatement(searchSelect);
        ResultSet results = session.execute(boundStatement.bind(
                searchSpace.name(),
                searchText1.toLowerCase(),
                searchText2));

        if (results == null || results.isExhausted()) {
            return null;
        }
        Row row = results.one();

        SearchResult result = new SearchResult();
        result.searchSpace = searchSpace;
        result.searchText1 = searchText1;
        result.searchText2 = searchText2;
        result.teamId = row.getString("teamId");
        result.serviceId = row.getString("serviceId");
        result.moduleId = row.getString("moduleId");
        result.hostId = row.getString("hostId");
        result.vipId = row.getString("vipId");

        return result;
    }

    @Override
    public List<SearchResult> doSearchList(SearchSpace searchSpace, String searchText1) {
        BoundStatement boundStatement = new BoundStatement(searchSelect2);
        ResultSet results = session.execute(boundStatement.bind(
                searchSpace.name(),
                searchText1.toLowerCase()));

        if (results == null || results.isExhausted()) {
            return null;
        }

        List<SearchResult> list = new LinkedList<>();
        for (Row row : results.all()) {
            SearchResult result = new SearchResult();
            result.searchSpace = searchSpace;
            result.searchText1 = searchText1;
            result.searchText2 = row.getString("searchText2");
            result.teamId = row.getString("teamId");
            result.serviceId = row.getString("serviceId");
            result.moduleId = row.getString("moduleId");
            result.hostId = row.getString("hostId");
            result.vipId = row.getString("vipId");
            list.add(result);
        }

        return list;
    }

    @Override
    public void insertSearch(SearchSpace searchSpace, String searchText1, String teamId, String serviceId, String moduleId, String hostId, String vipId) {
        insertSearch(searchSpace, searchText1, "-", teamId, serviceId, moduleId, hostId, vipId);
    }

    @Override
    public void insertSearch(SearchSpace searchSpace, String searchText1, String searchText2, String teamId, String serviceId, String moduleId, String hostId, String vipId) {
        SearchResult searchResult = doSearch(searchSpace, searchText1, searchText2);
        if (searchResult != null) {
            LOGGER.debug("Insert into search failed, record already exists s:{} t1:{} t2:{}",
                    searchSpace,
                    searchText1,
                    searchText2);
            return;
        }

        BoundStatement boundStatement = new BoundStatement(searchInsert);
        session.execute(boundStatement.bind(
                searchSpace.name(),
                searchText1.toLowerCase(),
                searchText2.toLowerCase(),
                teamId,
                serviceId,
                moduleId,
                hostId,
                vipId));
    }

    @Override
    public void deleteSearch(SearchSpace searchSpace, String searchText1) {
        deleteSearch(searchSpace, searchText1, "-");
    }

    @Override
    public void deleteSearch(SearchSpace searchSpace, String searchText1, String searchText2) {
        BoundStatement boundStatement = new BoundStatement(searchDelete);
        session.execute(boundStatement.bind(
                searchSpace.name(),
                searchText1.toLowerCase(),
                searchText2.toLowerCase()));
    }

    @Override
    public void updateStatus(String id, boolean busy, String status, String statusCode) {
        BoundStatement boundStatement = new BoundStatement(statusInsert);
        session.execute(boundStatement.bind(
                id,
                busy,
                status,
                statusCode));
    }

    private Row getStatus(String id) {
        BoundStatement boundStatement = new BoundStatement(statusSelect);
        ResultSet results = session.execute(boundStatement.bind(id));
        if (results == null || results.isExhausted()) {
            return null;
        }
        return results.one();
    }

    private <T extends Object> List<T> getData(String tableName, Class<T> classOfT) {
        ResultSet results = session.execute(CQL_SELECT_PRE + tableName + CQL_SELECT_POST);
        List<T> listOfData = new LinkedList<>();
        for (Row row : results) {
            String data = row.getString("data");
            listOfData.add(gson.fromJson(data, classOfT));
        }
        return listOfData;
    }

    private <T extends Object> T getData(String id, PreparedStatement statement, Class<T> classOfT) {
        BoundStatement boundStatement = new BoundStatement(statement);
        ResultSet results = session.execute(boundStatement.bind(id));
        for (Row row : results) {
            String data = row.getString("data");
            return gson.fromJson(data, classOfT);
        }
        return null;
    }

    private void saveData(String id, String data, PreparedStatement statement) {
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(id, data));
    }

    private void updateData(String id, String data, PreparedStatement statement) {
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(data, id));
    }

    private void deleteData(String id, PreparedStatement statement) {
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(id));
    }

    private <T extends Object> List<T> getServiceData(String serviceId, PreparedStatement statement, Class<T> classOfT) {
        BoundStatement boundStatement = new BoundStatement(statement);
        ResultSet results = session.execute(boundStatement.bind(serviceId));
        List<T> listOfData = new LinkedList<>();
        for (Row row : results) {
            String data = row.getString("data");
            listOfData.add(gson.fromJson(data, classOfT));
        }
        return listOfData;
    }

    private <T extends Object> T getServiceData(String serviceId, String id, PreparedStatement statement, Class<T> classOfT) {
        BoundStatement boundStatement = new BoundStatement(statement);
        ResultSet results = session.execute(boundStatement.bind(serviceId, id));
        for (Row row : results) {
            String data = row.getString("data");
            return gson.fromJson(data, classOfT);
        }
        return null;
    }

    private void saveServiceData(String serviceId, String id, String data, PreparedStatement statement) {
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(serviceId, id, data));
    }

    private void updateServiceData(String serviceId, String id, String data, PreparedStatement statement) {
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(data, serviceId, id));
    }

    private void deleteServiceData(String serviceId, String id, PreparedStatement statement) {
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(serviceId, id));
    }

    public void close() {
        session.close();
    }

}
