package com.northernwall.hadrian.db;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
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
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class CassandraDataAccess implements DataAccess {

    private final Cluster cluster;

    private final String cqlSelectPre;
    private final String cqlSelectPostAll;

    private final PreparedStatement customFunctionSelect;
    private final PreparedStatement customFunctionSelect2;
    private final PreparedStatement customFunctionInsert;
    private final PreparedStatement customFunctionDelete = null;
    private final PreparedStatement dataStoreSelect;
    private final PreparedStatement dataStoreSelect2;
    private final PreparedStatement dataStoreInsert;
    private final PreparedStatement dataStoreDelete = null;
    private final PreparedStatement hostSelect;
    private final PreparedStatement hostSelect2;
    private final PreparedStatement hostInsert;
    private final PreparedStatement hostDelete = null;
    private final PreparedStatement serviceSelect;
    private final PreparedStatement serviceInsert;
    private final PreparedStatement serviceDelete = null;
    private final PreparedStatement teamSelect;
    private final PreparedStatement teamInsert;
    private final PreparedStatement teamDelete = null;
    private final PreparedStatement userSelect;
    private final PreparedStatement userInsert;
    private final PreparedStatement userDelete = null;
    private final PreparedStatement userSessionSelect;
    private final PreparedStatement userSessionInsert;
    private final PreparedStatement userSessionDelete = null;
    private final PreparedStatement vipSelect;
    private final PreparedStatement vipSelect2;
    private final PreparedStatement vipInsert;
    private final PreparedStatement vipDelete = null;
    private final PreparedStatement workItemSelect;
    private final PreparedStatement workItemInsert;

    private final Gson gson;

    public CassandraDataAccess(Cluster cluster, String keyspace) {
        this.cluster = cluster;

        cqlSelectPre = "SELECT * FROM " + keyspace + ".";
        cqlSelectPostAll = ";";
        
        Session session = cluster.connect(keyspace);
        
        customFunctionSelect = session.prepare("SELECT * FROM " + keyspace + ".customFunction WHERE serviceId = ?;");
        customFunctionSelect2 = session.prepare("SELECT * FROM " + keyspace + ".customFunction WHERE serviceId = ? AND id = ?;");
        customFunctionInsert = session.prepare("INSERT INTO " + keyspace + ".customFunction (serviceId, id, data) VALUES (?, ?, ?);");
        dataStoreSelect = session.prepare("SELECT * FROM " + keyspace + ".dataStore WHERE serviceId = ?;");
        dataStoreSelect2 = session.prepare("SELECT * FROM " + keyspace + ".dataStore WHERE serviceId = ? AND id = ?;");
        dataStoreInsert = session.prepare("INSERT INTO " + keyspace + ".dataStore (serviceId, id, data) VALUES (?, ?, ?);");
        hostSelect = session.prepare("SELECT * FROM " + keyspace + ".host WHERE serviceId = ?;");
        hostSelect2 = session.prepare("SELECT * FROM " + keyspace + ".host WHERE serviceId = ? AND id = ?;");
        hostInsert = session.prepare("INSERT INTO " + keyspace + ".host (serviceId, id, data) VALUES (?, ?, ?);");
        serviceSelect = session.prepare("SELECT * FROM " + keyspace + ".service WHERE id = ?;");
        serviceInsert = session.prepare("INSERT INTO " + keyspace + ".service (id, data) VALUES (?, ?);");
        teamSelect = session.prepare("SELECT * FROM " + keyspace + ".team WHERE id = ?;");
        teamInsert = session.prepare("INSERT INTO " + keyspace + ".team (id, data) VALUES (?, ?);");
        userSelect = session.prepare("SELECT * FROM " + keyspace + ".user WHERE id = ?;");
        userInsert = session.prepare("INSERT INTO " + keyspace + ".user (id, data) VALUES (?, ?);");
        userSessionSelect = session.prepare("SELECT * FROM " + keyspace + ".userSession WHERE id = ?;");
        userSessionInsert = session.prepare("INSERT INTO " + keyspace + ".userSession (id, data) VALUES (?, ?);");
        vipSelect = session.prepare("SELECT * FROM " + keyspace + ".vip WHERE serviceId = ?;");
        vipSelect2 = session.prepare("SELECT * FROM " + keyspace + ".vip WHERE serviceId = ? AND id = ?;");
        vipInsert = session.prepare("INSERT INTO " + keyspace + ".vip (serviceId, id, data) VALUES (?, ?, ?);");
        workItemSelect = session.prepare("SELECT * FROM " + keyspace + ".workItem WHERE id = ?;");
        workItemInsert = session.prepare("INSERT INTO " + keyspace + ".workItem (id, data) VALUES (?, ?);");
       
        gson = new Gson();
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
        saveData(team.getTeamId(), gson.toJson(team), teamInsert);
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
        saveData(service.getServiceId(), gson.toJson(service), serviceInsert);
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
        saveServiceData(host.getServiceId(), host.getHostId(), gson.toJson(host), hostInsert);
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
        saveServiceData(vip.getServiceId(), vip.getVipId(), gson.toJson(vip), vipInsert);
    }

    @Override
    public void deleteVip(String serviceId, String vipId) {
        deleteServiceData(serviceId, vipId, vipDelete);
    }

    @Override
    public List<ServiceRef> getServiceRefs() {
        return new LinkedList<>();
    }

    @Override
    public List<ServiceRef> getServiceRefsByClient(String clientServiceId) {
        return new LinkedList<>();
    }

    @Override
    public List<ServiceRef> getServiceRefsByServer(String serverServiceId) {
        return new LinkedList<>();
    }

    @Override
    public void saveServiceRef(ServiceRef serviceRef) {
    }

    @Override
    public void deleteServiceRef(String clientId, String serviceId) {
    }

    @Override
    public List<VipRef> getVipRefsByHost(String hostId) {
        return new LinkedList<>();
    }

    @Override
    public VipRef getVipRef(String hostId, String vipId) {
        return null;
    }

    @Override
    public void saveVipRef(VipRef vipRef) {
    }

    @Override
    public void updateVipRef(VipRef vipRef) {
    }

    @Override
    public void deleteVipRef(String hostId, String vipId) {
    }

    @Override
    public void deleteVipRefs(String vipId) {
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
        saveServiceData(customFunction.getServiceId(), customFunction.getCustomFunctionId(), gson.toJson(customFunction), customFunctionInsert);
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
        saveServiceData(dataStore.getServiceId(), dataStore.getDataStoreId(), gson.toJson(dataStore), dataStoreInsert);
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
        saveData(user.getUsername(), gson.toJson(user), userInsert);
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
        Session session = cluster.connect();
        ResultSet results = session.execute(cqlSelectPre + tableName + cqlSelectPostAll);
        List<T> listOfData = new LinkedList<>();
        for (Row row : results) {
            String data = row.getString("data");
            listOfData.add(gson.fromJson(data, classOfT));
        }
        return listOfData;
    }

    private <T extends Object> T getData(String id, PreparedStatement statement, Class<T> classOfT) {
        Session session = cluster.connect();
        BoundStatement boundStatement = new BoundStatement(statement);
        ResultSet results = session.execute(boundStatement.bind(id));
        for (Row row : results) {
            String data = row.getString("data");
            return gson.fromJson(data, classOfT);
        }
        return null;
    }

    private void saveData(String id, String data, PreparedStatement statement) {
        Session session = cluster.connect();
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(id,data));
    }

    private void deleteData(String id, PreparedStatement statement) {
    }

    private <T extends Object> List<T> getServiceData(String serviceId, PreparedStatement statement, Class<T> classOfT) {
        Session session = cluster.connect();
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
        Session session = cluster.connect();
        BoundStatement boundStatement = new BoundStatement(statement);
        ResultSet results = session.execute(boundStatement.bind(serviceId, id));
        for (Row row : results) {
            String data = row.getString("data");
            return gson.fromJson(data, classOfT);
        }
        return null;
    }

    private void saveServiceData(String serviceId, String id, String data, PreparedStatement statement) {
        Session session = cluster.connect();
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(serviceId, id, data));
    }

    private void deleteServiceData(String serviceId, String id, PreparedStatement statement) {
    }

}