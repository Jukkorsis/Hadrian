/*
 * Copyright 2014 Richard Thurston.
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
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.UserSession;
import com.northernwall.hadrian.domain.WorkItem;
import java.util.List;

/**
 *
 * @author Richard Thurston
 */
public interface DataAccess {
    
    List<Team> getTeams();
    Team getTeam(String teamId);
    void saveTeam(Team team);
    void updateTeam(Team team);

    List<Service> getServices();
    List<Service> getServices(String teamId);
    Service getService(String serviceId);
    void saveService(Service service);
    void updateService(Service service);

    List<Host> getHosts(String serviceId);
    Host getHost(String serviceId, String hostId);
    void saveHost(Host host);
    void updateHost(Host host);
    void deleteHost(String serviceId, String hostId);

    List<Vip> getVips(String serviceId);
    Vip getVip(String serviceId, String vipId);
    void saveVip(Vip vip);
    void updateVip(Vip vip);
    void deleteVip(String serviceId, String vipId);

    List<ServiceRef> getServiceRefs();
    List<ServiceRef> getServiceRefsByClient(String clientServiceId);
    List<ServiceRef> getServiceRefsByServer(String serverServiceId);
    void saveServiceRef(ServiceRef serviceRef);
    void deleteServiceRef(String clientId, String serviceId);
    
    List<VipRef> getVipRefsByHost(String hostId);
    VipRef getVipRef(String hostId, String vipId);
    void saveVipRef(VipRef vipRef);
    void updateVipRef(VipRef vipRef);
    void deleteVipRef(String hostId, String vipId);
    void deleteVipRefs(String vipId);
    
    List<CustomFunction> getCustomFunctions(String serviceId);
    CustomFunction getCustomFunction(String serviceId, String customFunctionId);
    void saveCustomFunction(CustomFunction customFunction);
    void updateCustomFunction(CustomFunction customFunction);
    void deleteCustomFunction(String serviceId, String customFunctionId);

    List<DataStore> getDataStores(String serviceId);
    DataStore getDataStore(String serviceId, String dataStoreId);
    void saveDataStore(DataStore dataStore);
    void updateDataStore(DataStore dataStore);
    void deleteDataStore(String serviceId, String dataStoreId);
    
    List<User> getUsers();
    User getUser(String userName);
    void saveUser(User user);
    void updateUser(User user);
    void deleteUser(String userName);

    WorkItem getWorkItem(String id);
    void saveWorkItem(WorkItem workItem);

    UserSession getUserSession(String sessionId);
    void saveUserSession(UserSession userSession);
    void deleteUserSession(String sessionId);

}
