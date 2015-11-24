package com.northernwall.hadrian.db;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.gson.Gson;
import com.northernwall.hadrian.domain.CustomFunction;
import com.northernwall.hadrian.domain.DataStore;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceRef;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.UserSession;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.VipRef;
import com.northernwall.hadrian.domain.WorkItem;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraDataAccess implements DataAccess {

    private final static Logger logger = LoggerFactory.getLogger(CassandraDataAccess.class);

    private static final String CQL_SELECT_PRE = "SELECT * FROM ";
    private static final String CQL_SELECT_POST = ";";

    private final Session session;

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
    private final PreparedStatement serviceSelect;
    private final PreparedStatement serviceInsert;
    private final PreparedStatement serviceUpdate;
    private final PreparedStatement serviceRefSelectClient;
    private final PreparedStatement serviceRefSelectServer;
    private final PreparedStatement serviceRefInsertClient;
    private final PreparedStatement serviceRefInsertServer;
    private final PreparedStatement serviceRefDeleteClient;
    private final PreparedStatement serviceRefDeleteServer;
    private final PreparedStatement teamSelect;
    private final PreparedStatement teamInsert;
    private final PreparedStatement teamUpdate;
    private final PreparedStatement userSelect;
    private final PreparedStatement userInsert;
    private final PreparedStatement userUpdate;
    private final PreparedStatement userDelete;
    private final PreparedStatement userSessionSelect;
    private final PreparedStatement userSessionInsert;
    private final PreparedStatement userSessionDelete;
    private final PreparedStatement vipSelect;
    private final PreparedStatement vipSelect2;
    private final PreparedStatement vipInsert;
    private final PreparedStatement vipUpdate;
    private final PreparedStatement vipDelete;
    private final PreparedStatement vipRefSelectHost1;
    private final PreparedStatement vipRefSelectHost2;
    private final PreparedStatement vipRefSelectVip;
    private final PreparedStatement vipRefInsertHost;
    private final PreparedStatement vipRefInsertVip;
    private final PreparedStatement vipRefUpdateHost;
    private final PreparedStatement vipRefDeleteHost;
    private final PreparedStatement vipRefDeleteVip1;
    private final PreparedStatement vipRefDeleteVip2;
    private final PreparedStatement workItemSelect;
    private final PreparedStatement workItemInsert;

    private final Gson gson;

    public CassandraDataAccess(Cluster cluster, String keyspace) {
        session = cluster.connect(keyspace);

        customFunctionSelect = session.prepare("SELECT * FROM customFunction WHERE serviceId = ?;");
        customFunctionSelect2 = session.prepare("SELECT * FROM customFunction WHERE serviceId = ? AND id = ?;");
        customFunctionInsert = session.prepare("INSERT INTO customFunction (serviceId, id, data) VALUES (?, ?, ?);");
        customFunctionUpdate = session.prepare("UPDATE customFunction SET data = ? WHERE serviceId = ? AND id = ?;");
        customFunctionDelete = session.prepare("DELETE FROM customFunction WHERE serviceId = ? AND id = ?;");
        dataStoreSelect = session.prepare("SELECT * FROM dataStore WHERE serviceId = ?;");
        dataStoreSelect2 = session.prepare("SELECT * FROM dataStore WHERE serviceId = ? AND id = ?;");
        dataStoreInsert = session.prepare("INSERT INTO dataStore (serviceId, id, data) VALUES (?, ?, ?);");
        dataStoreUpdate = session.prepare("UPDATE dataStore SET data = ? WHERE serviceId = ? AND id = ?;");
        dataStoreDelete = session.prepare("DELETE FROM dataStore WHERE serviceId = ? AND id = ?;");
        hostSelect = session.prepare("SELECT * FROM host WHERE serviceId = ?;");
        hostSelect2 = session.prepare("SELECT * FROM host WHERE serviceId = ? AND id = ?;");
        hostInsert = session.prepare("INSERT INTO host (serviceId, id, data) VALUES (?, ?, ?);");
        hostUpdate = session.prepare("UPDATE host SET data = ? WHERE serviceId = ? AND id = ?;");
        hostDelete = session.prepare("DELETE FROM host WHERE serviceId = ? AND id = ?;");
        serviceSelect = session.prepare("SELECT * FROM service WHERE id = ?;");
        serviceInsert = session.prepare("INSERT INTO service (id, data) VALUES (?, ?);");
        serviceUpdate = session.prepare("UPDATE service SET data = ? WHERE id = ?;");
        serviceRefSelectClient = session.prepare("SELECT * FROM serviceRefClient WHERE clientServiceId = ?;");
        serviceRefSelectServer = session.prepare("SELECT * FROM serviceRefServer WHERE serverServiceId = ?;");
        serviceRefInsertClient = session.prepare("INSERT INTO serviceRefClient (clientServiceId, serverServiceId) VALUES (?, ?);");
        serviceRefInsertServer = session.prepare("INSERT INTO serviceRefServer (serverServiceId, clientServiceId) VALUES (?, ?);");
        serviceRefDeleteClient = session.prepare("DELETE FROM serviceRefClient WHERE clientServiceId = ? AND serverServiceId = ?;");
        serviceRefDeleteServer = session.prepare("DELETE FROM serviceRefServer WHERE serverServiceId = ? AND clientServiceId = ?;");
        teamSelect = session.prepare("SELECT * FROM team WHERE id = ?;");
        teamInsert = session.prepare("INSERT INTO team (id, data) VALUES (?, ?);");
        teamUpdate = session.prepare("UPDATE team SET data = ? WHERE id = ?;");
        userSelect = session.prepare("SELECT * FROM user WHERE id = ?;");
        userInsert = session.prepare("INSERT INTO user (id, data) VALUES (?, ?);");
        userUpdate = session.prepare("UPDATE user SET data = ? WHERE id = ?;");
        userDelete = session.prepare("DELETE FROM user WHERE id = ?;");
        userSessionSelect = session.prepare("SELECT * FROM userSession WHERE id = ?;");
        userSessionInsert = session.prepare("INSERT INTO userSession (id, data) VALUES (?, ?);");
        userSessionDelete = session.prepare("DELETE FROM userSession WHERE id = ?;");
        vipSelect = session.prepare("SELECT * FROM vip WHERE serviceId = ?;");
        vipSelect2 = session.prepare("SELECT * FROM vip WHERE serviceId = ? AND id = ?;");
        vipInsert = session.prepare("INSERT INTO vip (serviceId, id, data) VALUES (?, ?, ?);");
        vipUpdate = session.prepare("UPDATE vip SET data = ? WHERE serviceId = ? AND id = ?;");
        vipDelete = session.prepare("DELETE FROM vip WHERE serviceId = ? AND id = ?;");
        vipRefSelectHost1 = session.prepare("SELECT * FROM vipRefHost WHERE hostId = ?;");
        vipRefSelectHost2 = session.prepare("SELECT * FROM vipRefHost WHERE hostId = ? AND vipId = ?;");
        vipRefSelectVip = session.prepare("SELECT * FROM vipRefVip WHERE vipId = ?;");
        vipRefInsertHost = session.prepare("INSERT INTO vipRefHost (hostId, vipId, data) VALUES (?, ?, ?);");
        vipRefInsertVip = session.prepare("INSERT INTO vipRefVip (vipId, hostId) VALUES (?, ?);");
        vipRefUpdateHost = session.prepare("UPDATE vipRefHost SET data = ? WHERE hostId = ? AND vipId = ?;");
        vipRefDeleteHost = session.prepare("DELETE FROM vipRefHost WHERE hostId = ? AND vipId = ?;");
        vipRefDeleteVip1 = session.prepare("DELETE FROM vipRefVip WHERE vipId = ?;");
        vipRefDeleteVip2 = session.prepare("DELETE FROM vipRefVip WHERE vipId = ? AND hostId = ?;");
        workItemSelect = session.prepare("SELECT * FROM workItem WHERE id = ?;");
        workItemInsert = session.prepare("INSERT INTO workItem (id, data) VALUES (?, ?);");
        logger.info("Prapared statements created");

        gson = new Gson();
    }

    @Override
    public Map<String, String> getHealth() {
        Map<String, String> health = new HashMap<>();
        Metadata metadata = session.getCluster().getMetadata();
        health.put("Cass Cluster Name", metadata.getClusterName());
        health.put("Cass Session Keyspace", session.getLoggedKeyspace());
        int i = 1;
        for (com.datastax.driver.core.Host host : metadata.getAllHosts()) {
            health.put("Cass Host" + i + " Data Center:", host.getDatacenter());
            health.put("Cass Host" + i + " Address:", host.getAddress().getHostAddress());
            health.put("Cass Host" + i + " Rack:", host.getRack());
            health.put("Cass Host" + i + " State:", host.getState());
            health.put("Cass Host" + i + " Version:", host.getCassandraVersion().toString());
            i++;
        }
        return health;
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
    public List<Service> getServices() {
        return getData("service", Service.class);
    }

    @Override
    public List<Service> getServices(String teamId) {
        List<Service> services = getData("service", Service.class);
        services.removeIf(new Predicate<Service>() {
            @Override
            public boolean test(Service service) {
                return !service.getTeamId().equals(teamId);
            }
        });
        return services;
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
        return getServiceData(serviceId, hostSelect, Host.class);
    }

    @Override
    public Host getHost(String serviceId, String hostId) {
        return getServiceData(serviceId, hostId, hostSelect2, Host.class);
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
    public void deleteHost(String serviceId, String hostId) {
        deleteServiceData(serviceId, hostId, hostDelete);
    }

    @Override
    public List<Vip> getVips(String serviceId) {
        return getServiceData(serviceId, vipSelect, Vip.class);
    }

    @Override
    public Vip getVip(String serviceId, String vipId) {
        return getServiceData(serviceId, vipId, vipSelect2, Vip.class);
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
    public List<ServiceRef> getServiceRefs() {
        ResultSet results = session.execute(CQL_SELECT_PRE + "serviceRefClient" + CQL_SELECT_POST);
        List<ServiceRef> serviceRefs = new LinkedList<>();
        for (Row row : results) {
            serviceRefs.add(new ServiceRef(row.getString("clientServiceId"), row.getString("serverServiceId")));
        }
        return serviceRefs;
    }

    @Override
    public List<ServiceRef> getServiceRefsByClient(String clientServiceId) {
        BoundStatement boundStatement = new BoundStatement(serviceRefSelectClient);
        ResultSet results = session.execute(boundStatement.bind(clientServiceId));
        List<ServiceRef> serviceRefs = new LinkedList<>();
        for (Row row : results) {
            serviceRefs.add(new ServiceRef(row.getString("clientServiceId"), row.getString("serverServiceId")));
        }
        return serviceRefs;
    }

    @Override
    public List<ServiceRef> getServiceRefsByServer(String serverServiceId) {
        BoundStatement boundStatement = new BoundStatement(serviceRefSelectServer);
        ResultSet results = session.execute(boundStatement.bind(serverServiceId));
        List<ServiceRef> serviceRefs = new LinkedList<>();
        for (Row row : results) {
            serviceRefs.add(new ServiceRef(row.getString("clientServiceId"), row.getString("serverServiceId")));
        }
        return serviceRefs;
    }

    @Override
    public void saveServiceRef(ServiceRef serviceRef) {
        BoundStatement boundStatement;

        boundStatement = new BoundStatement(serviceRefInsertClient);
        session.execute(boundStatement.bind(serviceRef.getClientServiceId(), serviceRef.getServerServiceId()));

        boundStatement = new BoundStatement(serviceRefInsertServer);
        session.execute(boundStatement.bind(serviceRef.getServerServiceId(), serviceRef.getClientServiceId()));
    }

    @Override
    public void deleteServiceRef(String clientId, String serviceId) {
        BoundStatement boundStatement;

        boundStatement = new BoundStatement(serviceRefDeleteClient);
        session.execute(boundStatement.bind(clientId, serviceId));

        boundStatement = new BoundStatement(serviceRefDeleteServer);
        session.execute(boundStatement.bind(serviceId, clientId));
    }

    @Override
    public List<VipRef> getVipRefsByHost(String hostId) {
        BoundStatement boundStatement = new BoundStatement(vipRefSelectHost1);
        ResultSet results = session.execute(boundStatement.bind(hostId));
        List<VipRef> vipRefs = new LinkedList<>();
        for (Row row : results) {
            String data = row.getString("data");
            vipRefs.add(gson.fromJson(data, VipRef.class));
        }
        return vipRefs;
    }

    @Override
    public VipRef getVipRef(String hostId, String vipId) {
        BoundStatement boundStatement = new BoundStatement(vipRefSelectHost2);
        ResultSet results = session.execute(boundStatement.bind(hostId, vipId));
        for (Row row : results) {
            String data = row.getString("data");
            return gson.fromJson(data, VipRef.class);
        }
        return null;
    }

    @Override
    public void saveVipRef(VipRef vipRef) {
        BoundStatement boundStatement;

        boundStatement = new BoundStatement(vipRefInsertHost);
        session.execute(boundStatement.bind(vipRef.getHostId(), vipRef.getVipId(), gson.toJson(vipRef)));

        boundStatement = new BoundStatement(vipRefInsertVip);
        session.execute(boundStatement.bind(vipRef.getVipId(), vipRef.getHostId()));
    }

    @Override
    public void updateVipRef(VipRef vipRef) {
        BoundStatement boundStatement = new BoundStatement(vipRefUpdateHost);
        session.execute(boundStatement.bind(gson.toJson(vipRef), vipRef.getHostId(), vipRef.getVipId()));
    }

    @Override
    public void deleteVipRef(String hostId, String vipId) {
        BoundStatement boundStatement;

        boundStatement = new BoundStatement(vipRefDeleteHost);
        session.execute(boundStatement.bind(hostId, vipId));

        boundStatement = new BoundStatement(vipRefDeleteVip2);
        session.execute(boundStatement.bind(vipId, hostId));
    }

    @Override
    public void deleteVipRefs(String vipId) {
        BoundStatement boundStatement;

        boundStatement = new BoundStatement(vipRefSelectVip);
        ResultSet results = session.execute(boundStatement.bind(vipId));
        List<String> hostIds = new LinkedList<>();
        for (Row row : results) {
            hostIds.add(row.getString("hostId"));
        }

        for (String hostId : hostIds) {
            boundStatement = new BoundStatement(vipRefDeleteHost);
            session.execute(boundStatement.bind(hostId, vipId));
        }

        boundStatement = new BoundStatement(vipRefDeleteVip1);
        session.execute(boundStatement.bind(vipId));
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
    public List<User> getUsers() {
        return getData("user", User.class);
    }

    @Override
    public User getUser(String userName) {
        return getData(userName, userSelect, User.class);
    }

    @Override
    public void saveUser(User user) {
        saveData(user.getUsername(), gson.toJson(user), userInsert);
    }

    @Override
    public void updateUser(User user) {
        updateData(user.getUsername(), gson.toJson(user), userUpdate);
    }

    @Override
    public void deleteUser(String userName) {
        deleteData(userName, userDelete);
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
    public UserSession getUserSession(String sessionId) {
        return getData(sessionId, userSessionSelect, UserSession.class);
    }

    @Override
    public void saveUserSession(UserSession userSession) {
        saveData(userSession.getSessionId(), gson.toJson(userSession), userSessionInsert);
    }

    @Override
    public void deleteUserSession(String sessionId) {
        deleteData(sessionId, userSessionDelete);
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
