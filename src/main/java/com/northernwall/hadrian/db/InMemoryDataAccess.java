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

import com.northernwall.hadrian.domain.CustomFunction;
import com.northernwall.hadrian.domain.DataStore;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.VipRef;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceRef;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.UserSession;
import com.northernwall.hadrian.domain.WorkItem;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class InMemoryDataAccess implements DataAccess {
    private final static Logger logger = LoggerFactory.getLogger(InMemoryDataAccess.class);
    
    private final Map<String, Team> teams;
    private final Map<String, Service> services;
    private final Map<String, Host> hosts;
    private final Map<String, Vip> vips;
    private final List<ServiceRef> serviceRefs;
    private final List<VipRef> vipRefs;
    private final Map<String, CustomFunction> customFunctions;
    private final Map<String, DataStore> dataStores;
    private final Map<String, WorkItem> workItems;
    private final Map<String, UserSession> userSessions;

    public InMemoryDataAccess() {
        teams = new ConcurrentHashMap<>();
        services = new ConcurrentHashMap<>();
        hosts = new ConcurrentHashMap<>();
        vips = new ConcurrentHashMap<>();
        serviceRefs = new LinkedList<>();
        vipRefs = new LinkedList<>();
        customFunctions = new ConcurrentHashMap<>();
        dataStores = new ConcurrentHashMap<>();
        workItems = new ConcurrentHashMap<>();
        userSessions = new ConcurrentHashMap<>();
    }

    @Override
    public List<Team> getTeams() {
        List<Team> temp = new LinkedList<>(teams.values());
        Collections.sort(temp);
        return temp;
    }

    @Override
    public Team getTeam(String teamId) {
        return teams.get(teamId);
    }

    @Override
    public void saveTeam(Team team) {
        teams.put(team.getTeamId(), team);
    }

    @Override
    public List<Service> getServices() {
        List<Service> temp = new LinkedList<>();
        for (Service service : services.values()) {
            temp.add(service);
        }
        Collections.sort(temp);
        return temp;
    }

    @Override
    public List<Service> getServices(String teamId) {
        List<Service> temp = new LinkedList<>();
        for (Service service : services.values()) {
            if (service.getTeamId().equals(teamId)) {
                temp.add(service);
            }
        }
        Collections.sort(temp);
        return temp;
    }

    @Override
    public Service getService(String serviceId) {
        return services.get(serviceId);
    }

    @Override
    public void saveService(Service service) {
        services.put(service.getServiceId(), service);
    }

    @Override
    public void updateService(Service service) {
        services.put(service.getServiceId(), service);
    }

    @Override
    public List<Host> getHosts(String serviceId) {
        List<Host> temp = new LinkedList<>();
        for (Host host : hosts.values()) {
            if (host.getServiceId().equals(serviceId)) {
                temp.add(host);
            }
        }
        Collections.sort(temp);
        return temp;
    }

    @Override
    public Host getHost(String hostId) {
        return hosts.get(hostId);
    }

    @Override
    public void saveHost(Host host) {
        hosts.put(host.getHostId(), host);
    }
    
    @Override
    public void updateHost(Host host) {
        hosts.put(host.getHostId(), host);
    }
    
    @Override
    public void deleteHost(String hostId) {
        hosts.remove(hostId);
    }

    @Override
    public List<Vip> getVips(String serviceId) {
        List<Vip> temp = new LinkedList<>();
        for (Vip vip : vips.values()) {
            if (vip.getServiceId().equals(serviceId)) {
                temp.add(vip);
            }
        }
        Collections.sort(temp);
        return temp;
    }

    @Override
    public Vip getVip(String vipId) {
        return vips.get(vipId);
    }

    @Override
    public void saveVip(Vip vip) {
        vips.put(vip.getVipId(), vip);
    }
    
    @Override
    public void updateVip(Vip vip){
        vips.put(vip.getVipId(), vip);
    }
    
    @Override
    public void deleteVip(String vipId) {
        vips.remove(vipId);
    }

    @Override
    public List<ServiceRef> getServiceRefs() {
        return serviceRefs;
    }

    @Override
    public List<ServiceRef> getServiceRefsByClient(String clientServiceId) {
        List<ServiceRef> temp = new LinkedList<>();
        for (ServiceRef serviceRef : serviceRefs) {
            if (serviceRef.getClientServiceId().equals(clientServiceId)) {
                temp.add(serviceRef);
            }            
        }
        return temp;
    }

    @Override
    public List<ServiceRef> getServiceRefsByServer(String serverServiceId) {
        List<ServiceRef> temp = new LinkedList<>();
        for (ServiceRef serviceRef : serviceRefs) {
            if (serviceRef.getServerServiceId().equals(serverServiceId)) {
                temp.add(serviceRef);
            }            
        }
        return temp;
    }

    @Override
    public void saveServiceRef(ServiceRef serviceRef) {
        serviceRefs.add(serviceRef);
    }

    @Override
    public void deleteServiceRef(final String clientId, final String serviceId) {
        serviceRefs.removeIf(new Predicate<ServiceRef>() {
            @Override
            public boolean test(ServiceRef t) {
                return t.getClientServiceId().equals(clientId) && t.getServerServiceId().equals(serviceId);
            }
        });
    }

    @Override
    public List<VipRef> getVipRefsByHost(String hostId) {
        List<VipRef> temp = new LinkedList<>();
        for (VipRef vipRef : vipRefs) {
            if (vipRef.getHostId().equals(hostId)) {
                temp.add(vipRef);
            }            
        }
        return temp;
    }

    @Override
    public List<VipRef> getVipRefsByVip(String vipId) {
        List<VipRef> temp = new LinkedList<>();
        for (VipRef vipRef : vipRefs) {
            if (vipRef.getVipId().equals(vipId)) {
                temp.add(vipRef);
            }            
        }
        return temp;
    }

    @Override
    public VipRef getVipRef(String hostId, String vipId) {
        for (VipRef vipRef : vipRefs) {
            if (vipRef.getHostId().equals(hostId) && vipRef.getVipId().equals(vipId)) {
                return vipRef;
            }            
        }
        return null;
    }

    @Override
    public void saveVipRef(VipRef vipRef) {
        for (VipRef temp : vipRefs) {
            if (temp.getVipId().equals(vipRef.getVipId()) && temp.getHostId().equals(vipRef.getHostId())) {
                return;
            }            
        }
        vipRefs.add(vipRef);
    }
    
    @Override
    public void updateVipRef(VipRef vipRef) {
        for (VipRef temp : vipRefs) {
            if (temp.getVipId().equals(vipRef.getVipId()) && temp.getHostId().equals(vipRef.getHostId())) {
                temp.setStatus(vipRef.getStatus());
                return;
            }            
        }
    }
    
    @Override
    public void deleteVipRef(final String hostId, final String vipId) {
        vipRefs.removeIf(new Predicate<VipRef>() {
            @Override
            public boolean test(VipRef ref) {
                return ref.getHostId().equals(hostId) && ref.getVipId().equals(vipId);
            }
        });
    }

    @Override
    public void deleteVipRefs(final String vipId) {
        vipRefs.removeIf(new Predicate<VipRef>() {
            @Override
            public boolean test(VipRef ref) {
                return ref.getVipId().equals(vipId);
            }
        });
    }

    @Override
    public List<CustomFunction> getCustomFunctions(String serviceId) {
        List<CustomFunction> temp = new LinkedList<>();
        for (CustomFunction customFunction : customFunctions.values()) {
            if (customFunction.getServiceId().equals(serviceId)) {
                temp.add(customFunction);
            }
        }
        Collections.sort(temp);
        return temp;
    }
    
    @Override
    public CustomFunction getCustomFunction(String customFunctionId) {
        return customFunctions.get(customFunctionId);
    }
    
    @Override
    public void saveCustomFunction(CustomFunction customFunction) {
        customFunctions.put(customFunction.getCustomFunctionId(), customFunction);
    }
    
    @Override
    public void updateCustomFunction(CustomFunction customFunction) {
        customFunctions.put(customFunction.getCustomFunctionId(), customFunction);
    }
    
    @Override
    public void deleteCustomFunction(String customFunctionId) {
        customFunctions.remove(customFunctionId);
    }
    
    @Override
    public List<DataStore> getDataStores(String serviceId) {
        List<DataStore> temp = new LinkedList<>();
        for (DataStore dataStore : dataStores.values()) {
            if (dataStore.getServiceId().equals(serviceId)) {
                temp.add(dataStore);
            }
        }
        Collections.sort(temp);
        return temp;
    }

    @Override
    public DataStore getDataStore(String dataStoreId) {
        return dataStores.get(dataStoreId);
    }

    @Override
    public void saveDataStore(DataStore dataStore) {
        dataStores.put(dataStore.getDataStoreId(), dataStore);
    }

    @Override
    public void updateDataStore(DataStore dataStore) {
        dataStores.put(dataStore.getDataStoreId(), dataStore);
    }

    @Override
    public void deleteDataStore(String dataStoreId) {
        dataStores.remove(dataStoreId);
    }

    @Override
    public void saveWorkItem(WorkItem workItem) {
        workItems.put(workItem.getId(), workItem);
    }
    
    @Override
    public WorkItem getWorkItem(String id) {
        return workItems.get(id);
    }

    @Override
    public UserSession getUserSession(String sessionId) {
        return userSessions.get(sessionId);
    }

    @Override
    public void saveUserSession(UserSession userSession) {
        userSessions.put(userSession.getSessionId(), userSession);
    }

}
