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

import com.northernwall.hadrian.domain.Audit;
import com.northernwall.hadrian.domain.CustomFunction;
import com.northernwall.hadrian.domain.DataStore;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.ModuleFile;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ModuleRef;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.User;
import com.northernwall.hadrian.domain.WorkItem;
import com.northernwall.hadrian.handlers.utility.HealthWriter;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Richard Thurston
 */
public interface DataAccess {
    String getVersion();
    void setVersion(String version);
    
    void getHealth(HealthWriter writer) throws IOException;

    List<Team> getTeams();
    Team getTeam(String teamId);
    void saveTeam(Team team);
    void updateTeam(Team team);

    List<Service> getAllServices();
    List<Service> getActiveServices();
    Service getService(String serviceId);
    void saveService(Service service);
    void updateService(Service service);

    void backfillService(Service service);

    List<Host> getHosts(String serviceId);
    Host getHostByHostName(String hostName);
    Host getHost(String serviceId, String hostId);
    void saveHost(Host host);
    void updateHost(Host host);
    void deleteHost(Host host);

    void backfillHostName(Host host);

    List<Vip> getVips(String serviceId);
    Vip getVip(String serviceId, String vipId);
    void saveVip(Vip vip);
    void updateVip(Vip vip);
    void deleteVip(String serviceId, String vipId);

    List<ModuleRef> getModuleRefs();
    List<ModuleRef> getModuleRefsByClient(String clientServiceId, String clientModuleId);
    List<ModuleRef> getModuleRefsByServer(String serverServiceId, String serverModuleId);
    void saveModuleRef(ModuleRef moduleRef);
    void deleteModuleRef(String clientServiceId, String clientModuleId, String serverServiceId, String serverModuleId);
    
    List<CustomFunction> getCustomFunctions(String serviceId);
    CustomFunction getCustomFunction(String serviceId, String customFunctionId);
    void saveCustomFunction(CustomFunction customFunction);
    void updateCustomFunction(CustomFunction customFunction);
    void deleteCustomFunction(String serviceId, String customFunctionId);

    List<Module> getModules(String serviceId);
    Module getModule(String serviceId, String moduleId);
    void saveModule(Module module);
    void updateModule(Module module);
    void deleteModule(String serviceId, String moduleId);
    
    List<ModuleFile> getModuleFiles(String serviceId, String moduleId, String network);
    ModuleFile getModuleFile(String serviceId, String moduleId, String network, String name);
    void saveModuleFile(ModuleFile moduleFile);
    void updateModuleFile(ModuleFile moduleFile);
    void deleteModuleFile(String serviceId, String moduleId, String network, String name);

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

    List<WorkItem> getWorkItems();
    WorkItem getWorkItem(String id);
    void saveWorkItem(WorkItem workItem);
    void deleteWorkItem(String id);

    public int getWorkItemStatus(String id);
    public void saveWorkItemStatus(String id, int i);

    void saveAudit(Audit audit, String output);
    List<Audit> getAudit(String serviceId, int year, int month, int startDay, int endDay);
    public String getAuditOutput(String serviceId, String auditId);

    public boolean getAvailability();

}
