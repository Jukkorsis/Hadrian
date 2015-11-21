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

public class CassandraDataAccess implements DataAccess {

    private final Cluster cluster;

    private final String cqlServiceSelect;
    private final String cqlServiceSelectBy;
    private final String cqlServiceInsert;

    private final String cqlTeamSelect;
    private final String cqlTeamSelectBy;
    private final String cqlTeamInsert;

    private final String cqlUserSelect;
    private final String cqlUserSelectBy;
    private final String cqlUserInsert;

    private final Gson gson;

    public CassandraDataAccess(Cluster cluster, String keyspace) {
        this.cluster = cluster;

        cqlServiceSelect = "SELECT * FROM " + keyspace + ".servuce;";
        cqlServiceSelectBy = "SELECT * FROM " + keyspace + ".servuce WHERE servuceId = ?;";
        cqlServiceInsert = "INSERT INTO " + keyspace + ".servuce (serviceId, serviceAbbr, serviceName, teamId, description, runAs, gitPath, mavenGroupId, mavenArtifactId, versionUrl, availabilityUrl, startCmdLine, stopCmdLine) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        cqlTeamSelect = "SELECT * FROM " + keyspace + ".team;";
        cqlTeamSelectBy = "SELECT * FROM " + keyspace + ".team WHERE teamId = ?;";
        cqlTeamInsert = "INSERT INTO " + keyspace + ".team (teamId, teamName, usernames) VALUES (?, ?, ?, ?);";

        cqlUserSelect = "SELECT * FROM " + keyspace + ".user;";
        cqlUserSelectBy = "SELECT * FROM " + keyspace + ".user WHERE username = ?;";
        cqlUserInsert = "INSERT INTO " + keyspace + ".user (username, fullName, ops, admin) VALUES (?, ?, ?, ?);";

        gson = new Gson();
    }

    @Override
    public List<Team> getTeams() {
        Session session = cluster.connect();
        ResultSet results = session.execute(cqlTeamSelect);
        List<Team> teams = new LinkedList<>();
        for (Row row : results) {
            Team team = new Team(
                    row.getString("teamId"),
                    row.getString("teamName"),
                    gson.fromJson(row.getString("usernames"), List.class));
            teams.add(team);
        }
        return teams;
    }

    @Override
    public Team getTeam(String teamId) {
        Session session = cluster.connect();
        PreparedStatement statement = session.prepare(cqlTeamSelectBy);
        BoundStatement boundStatement = new BoundStatement(statement);
        ResultSet results = session.execute(boundStatement.bind(teamId));
        for (Row row : results) {
            return new Team(
                    row.getString("teamId"),
                    row.getString("teamName"),
                    gson.fromJson(row.getString("usernames"), List.class));
        }
        return null;
    }

    @Override
    public void saveTeam(Team team) {
        Session session = cluster.connect();
        PreparedStatement statement = session.prepare(cqlTeamInsert);
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(
                team.getTeamId(),
                team.getTeamName(),
                gson.toJson(team.getUsernames())));
    }

    @Override
    public void updateTeam(Team team) {
        saveTeam(team);
    }

    @Override
    public List<Service> getServices() {
        Session session = cluster.connect();
        ResultSet results = session.execute(cqlServiceSelect);
        List<Service> services = new LinkedList<>();
        for (Row row : results) {
            Service service = new Service(
                    row.getString("serviceId"),
                    row.getString("serviceAbbr"),
                    row.getString("serviceName"),
                    row.getString("teamId"),
                    row.getString("description"),
                    row.getString("runAs"),
                    row.getString("gitPath"),
                    row.getString("mavenGroupId"),
                    row.getString("mavenArtifactId"),
                    row.getString("versionUrl"),
                    row.getString("availabilityUrl"),
                    row.getString("startCmdLine"),
                    row.getString("stopCmdLine"));
            services.add(service);
        }
        return services;
    }

    @Override
    public List<Service> getServices(String teamId) {
        Session session = cluster.connect();
        ResultSet results = session.execute(cqlServiceSelect);
        List<Service> services = new LinkedList<>();
        for (Row row : results) {
            String temp = row.getString("teamId");
            if (teamId.equals(temp)) {
                Service service = new Service(
                        row.getString("serviceId"),
                        row.getString("serviceAbbr"),
                        row.getString("serviceName"),
                        temp,
                        row.getString("description"),
                        row.getString("runAs"),
                        row.getString("gitPath"),
                        row.getString("mavenGroupId"),
                        row.getString("mavenArtifactId"),
                        row.getString("versionUrl"),
                        row.getString("availabilityUrl"),
                        row.getString("startCmdLine"),
                        row.getString("stopCmdLine"));
                services.add(service);
            }
        }
        return services;
    }

    @Override
    public Service getService(String serviceId) {
        Session session = cluster.connect();
        PreparedStatement statement = session.prepare(cqlTeamSelectBy);
        BoundStatement boundStatement = new BoundStatement(statement);
        ResultSet results = session.execute(boundStatement.bind(serviceId));
        for (Row row : results) {
            return new Service(
                    row.getString("serviceId"),
                    row.getString("serviceAbbr"),
                    row.getString("serviceName"),
                    row.getString("teamId"),
                    row.getString("description"),
                    row.getString("runAs"),
                    row.getString("gitPath"),
                    row.getString("mavenGroupId"),
                    row.getString("mavenArtifactId"),
                    row.getString("versionUrl"),
                    row.getString("availabilityUrl"),
                    row.getString("startCmdLine"),
                    row.getString("stopCmdLine"));
        }
        return null;
    }

    @Override
    public void saveService(Service service) {
        Session session = cluster.connect();
        PreparedStatement statement = session.prepare(cqlServiceInsert);
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(
                service.getServiceId(),
                service.getServiceAbbr(),
                service.getServiceName(),
                service.getTeamId(),
                service.getDescription(),
                service.getRunAs(),
                service.getGitPath(),
                service.getMavenGroupId(),
                service.getMavenArtifactId(),
                service.getVersionUrl(),
                service.getAvailabilityUrl(),
                service.getStartCmdLine(),
                service.getStopCmdLine()));
    }

    @Override
    public void updateService(Service service) {
        saveService(service);
    }

    @Override
    public List<Host> getHosts(String serviceId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Host getHost(String hostId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveHost(Host host) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateHost(Host host) {
        saveHost(host);
    }

    @Override
    public void deleteHost(String hostId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Vip> getVips(String serviceId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Vip getVip(String vipId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveVip(Vip vip) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateVip(Vip vip) {
        saveVip(vip);
    }

    @Override
    public void deleteVip(String vipId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ServiceRef> getServiceRefs() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ServiceRef> getServiceRefsByClient(String clientServiceId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ServiceRef> getServiceRefsByServer(String serverServiceId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveServiceRef(ServiceRef serviceRef) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteServiceRef(String clientId, String serviceId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<VipRef> getVipRefsByHost(String instanceId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<VipRef> getVipRefsByVip(String vipId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public VipRef getVipRef(String hostId, String vipId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveVipRef(VipRef vipRef) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateVipRef(VipRef vipRef) {
        saveVipRef(vipRef);
    }

    @Override
    public void deleteVipRef(String hostId, String vipId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteVipRefs(String vipId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<CustomFunction> getCustomFunctions(String serviceId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CustomFunction getCustomFunction(String customFunctionId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveCustomFunction(CustomFunction customFunction) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateCustomFunction(CustomFunction customFunction) {
        saveCustomFunction(customFunction);
    }

    @Override
    public void deleteCustomFunction(String customFunctionId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<DataStore> getDataStores(String serviceId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataStore getDataStore(String dataStoreId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveDataStore(DataStore dataStore) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateDataStore(DataStore dataStore) {
        saveDataStore(dataStore);
    }

    @Override
    public void deleteDataStore(String dataStoreId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<User> getUsers() {
        Session session = cluster.connect();
        ResultSet results = session.execute(cqlUserSelect);
        List<User> users = new LinkedList<>();
        for (Row row : results) {
            User user = new User(
                    row.getString("username"),
                    row.getString("fullName"),
                    row.getBool("ops"),
                    row.getBool("admin"));
            users.add(user);
        }
        return users;
    }

    @Override
    public User getUser(String userName) {
        Session session = cluster.connect();
        PreparedStatement statement = session.prepare(cqlUserSelectBy);
        BoundStatement boundStatement = new BoundStatement(statement);
        ResultSet results = session.execute(boundStatement.bind(userName));
        for (Row row : results) {
            User user = new User(
                    row.getString("username"),
                    row.getString("fullName"),
                    row.getBool("ops"),
                    row.getBool("admin"));
            return user;
        }
        return null;
    }

    @Override
    public void saveUser(User user) {
        Session session = cluster.connect();
        PreparedStatement statement = session.prepare(cqlUserInsert);
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(
                user.getUsername(),
                user.getFullName(),
                user.isOps(),
                user.isAdmin()));
    }

    @Override
    public void updateUser(User user) {
        saveUser(user);
    }

    @Override
    public void deleteUser(String userName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveWorkItem(WorkItem workItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public WorkItem getWorkItem(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public UserSession getUserSession(String sessionId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveUserSession(UserSession userSession) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteUserSession(String sessionId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
