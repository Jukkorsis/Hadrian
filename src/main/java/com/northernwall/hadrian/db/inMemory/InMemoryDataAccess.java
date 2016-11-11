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
package com.northernwall.hadrian.db.inMemory;

import com.northernwall.hadrian.db.DataAccess;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Richard Thurston
 */
public class InMemoryDataAccess implements DataAccess {

    private String version;
    private final Map<String, Team> teams;
    private final Map<String, Service> services;
    private final Map<String, Host> hosts;
    private final Map<String, Vip> vips;
    private final List<ModuleRef> moduleRefs;
    private final Map<String, CustomFunction> customFunctions;
    private final Map<String, Module> modules;
    private final Map<String, DataStore> dataStores;
    private final Map<String, WorkItem> workItems;
    private final Map<String, User> users;
    private final List<Audit> audits;
    private final Map<String, ModuleFile> moduleFiles;

    public InMemoryDataAccess() {
        version = null;
        teams = new ConcurrentHashMap<>();
        services = new ConcurrentHashMap<>();
        hosts = new ConcurrentHashMap<>();
        vips = new ConcurrentHashMap<>();
        moduleRefs = new LinkedList<>();
        customFunctions = new ConcurrentHashMap<>();
        modules = new ConcurrentHashMap<>();
        dataStores = new ConcurrentHashMap<>();
        workItems = new ConcurrentHashMap<>();
        users = new ConcurrentHashMap<>();
        audits = new LinkedList<>();
        moduleFiles = new ConcurrentHashMap<>();
    }

    @Override
    public boolean getAvailability() {
        return true;
    }

    @Override
    public void getHealth(HealthWriter writer) {
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
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
    public void updateTeam(Team team) {
        teams.put(team.getTeamId(), team);
    }

    @Override
    public List<Service> getAllServices() {
        List<Service> temp = new LinkedList<>();
        for (Service service : services.values()) {
            temp.add(service);
        }
        Collections.sort(temp);
        return temp;
    }

    @Override
    public List<Service> getActiveServices() {
        List<Service> temp = new LinkedList<>();
        for (Service service : services.values()) {
            if (service.isActive()) {
                temp.add(service);
            }
        }
        Collections.sort(temp);
        return temp;
    }

    @Override
    public Service getServiceByServiceName(String serviceName) {
        for (Service service : services.values()) {
            if (service.isActive() && service.getServiceName().equalsIgnoreCase(serviceName)) {
                return service;
            }
        }
        return null;
    }

    @Override
    public Service getServiceByGitProject(String gitProject) {
        for (Service service : services.values()) {
            if (service.isActive() && service.getGitProject().equalsIgnoreCase(gitProject)) {
                return service;
            }
        }
        return null;
    }

    @Override
    public Service getServiceByMavenGroup(String mavenGroupId) {
        for (Service service : services.values()) {
            if (service.isActive() && service.getMavenGroupId().equalsIgnoreCase(mavenGroupId)) {
                return service;
            }
        }
        return null;
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
    public void deleteServiceSearch(Service service) {
        services.put(service.getServiceId(), service);
    }

    @Override
    public void insertServiceSearch(Service service) {
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
    public Host getHostByHostName(String hostName) {
        for (Host host : hosts.values()) {
            if (host.getHostName().equalsIgnoreCase(hostName)) {
                return host;
            }
        }
        return null;
    }

    @Override
    public Host getHost(String serviceId, String hostId) {
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
    public void deleteHost(Host host) {
        hosts.remove(host.getHostId());
    }

    @Override
    public void backfillHostName(Host host) {
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
    public Vip getVip(String serviceId, String vipId) {
        return vips.get(vipId);
    }

    @Override
    public void saveVip(Vip vip) {
        vips.put(vip.getVipId(), vip);
    }

    @Override
    public void updateVip(Vip vip) {
        vips.put(vip.getVipId(), vip);
    }

    @Override
    public void deleteVip(String serviceId, String vipId) {
        vips.remove(vipId);
    }

    @Override
    public List<ModuleRef> getModuleRefs() {
        return moduleRefs;
    }

    @Override
    public List<ModuleRef> getModuleRefsByClient(String clientServiceId, String clientModuleId) {
        List<ModuleRef> temp = new LinkedList<>();
        for (ModuleRef moduleRef : moduleRefs) {
            if (moduleRef.getClientServiceId().equals(clientServiceId) && moduleRef.getClientModuleId().equals(clientModuleId)) {
                temp.add(moduleRef);
            }
        }
        return temp;
    }

    @Override
    public List<ModuleRef> getModuleRefsByServer(String serverServiceId, String serverModuleId) {
        List<ModuleRef> temp = new LinkedList<>();
        for (ModuleRef moduleRef : moduleRefs) {
            if (moduleRef.getServerServiceId().equals(serverServiceId) && moduleRef.getServerModuleId().equals(serverModuleId)) {
                temp.add(moduleRef);
            }
        }
        return temp;
    }

    @Override
    public void saveModuleRef(ModuleRef moduleRef) {
        moduleRefs.add(moduleRef);
    }

    @Override
    public void deleteModuleRef(String clientServiceId, String clientModuleId, String serverServiceId, String serverModuleId) {
        moduleRefs.removeIf(new ModuleRefPredicate(clientServiceId, clientModuleId, serverServiceId, serverModuleId));
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
    public CustomFunction getCustomFunction(String serviceId, String customFunctionId) {
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
    public void deleteCustomFunction(String serviceId, String customFunctionId) {
        customFunctions.remove(customFunctionId);
    }

    @Override
    public List<Module> getModules(String serviceId) {
        List<Module> temp = new LinkedList<>();
        for (Module module : modules.values()) {
            if (module.getServiceId().equals(serviceId)) {
                temp.add(module);
            }
        }
        Collections.sort(temp);
        return temp;
    }

    @Override
    public Module getModule(String serviceId, String moduleId) {
        return modules.get(moduleId);
    }

    @Override
    public void saveModule(Module module) {
        modules.put(module.getModuleId(), module);
    }

    @Override
    public void updateModule(Module module) {
        modules.put(module.getModuleId(), module);
    }

    @Override
    public void deleteModule(String serviceId, String moduleId) {
        modules.remove(moduleId);
    }

    @Override
    public List<ModuleFile> getModuleFiles(String serviceId, String moduleId, String environment) {
        List<ModuleFile> moduleFileList = new ArrayList<>();

        for (ModuleFile moduleFile : moduleFiles.values()) {
            if (moduleFile.getServiceId().equals(serviceId) &&
                    moduleFile.getModuleId().equals(moduleId) &&
                    moduleFile.getEnvironment().equals(environment)) {
                moduleFileList.add(moduleFile);
            }
        }

        return moduleFileList;
    }

    @Override
    public ModuleFile getModuleFile(String serviceId, String moduleId, String environment, String name) {
        for (ModuleFile moduleFile : moduleFiles.values()) {
            if (moduleFile.getServiceId().equals(serviceId) &&
                    moduleFile.getModuleId().equals(moduleId) &&
                    moduleFile.getEnvironment().equals(environment) &&
                    moduleFile.getName().equals(name)) {
               return moduleFile;
            }
        }
        return null;
    }
    
    @Override
    public void saveModuleFile(ModuleFile moduleFile) {
        moduleFiles.put(String.valueOf(moduleFile.hashCode()), moduleFile);
    }
    
    @Override
    public void updateModuleFile(ModuleFile moduleFile) {
    }
    
    @Override
    public void deleteModuleFile(String serviceId, String moduleId, String environment, String name) {
        for (Iterator<Map.Entry<String, ModuleFile>> it = moduleFiles.entrySet().iterator(); it.hasNext();) {
            ModuleFile moduleFile = it.next().getValue();
            if (moduleFile.getServiceId().equals(serviceId) &&
                    moduleFile.getModuleId().equals(moduleId) &&
                    moduleFile.getEnvironment().equals(environment) &&
                    moduleFile.getName().equals(name)) {
                it.remove();
            }
        }
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
    public DataStore getDataStore(String serviceId, String dataStoreId) {
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
    public void deleteDataStore(String serviceId, String dataStoreId) {
        dataStores.remove(dataStoreId);
    }

    @Override
    public List<WorkItem> getWorkItems() {
        List<WorkItem> temp = new LinkedList<>(workItems.values());
        return temp;
    }

    @Override
    public WorkItem getWorkItem(String id) {
        return workItems.get(id);
    }

    @Override
    public void saveWorkItem(WorkItem workItem) {
        workItems.put(workItem.getId(), workItem);
    }

    @Override
    public void deleteWorkItem(String id) {
        workItems.remove(id);

    }

    @Override
    public int getWorkItemStatus(String id) {
        return 200;
    }

    @Override
    public void saveWorkItemStatus(String id, int status) {
    }

    @Override
    public List<User> getUsers() {
        List<User> temp = new LinkedList<>(users.values());
        Collections.sort(temp);
        return temp;
    }

    @Override
    public User getUser(String userName) {
        return users.get(userName);
    }

    @Override
    public void saveUser(User user) {
        users.put(user.getUsername(), user);
    }

    @Override
    public void updateUser(User user) {
        users.put(user.getUsername(), user);
    }

    @Override
    public void deleteUser(String userName) {
        users.remove(userName);
    }

    @Override
    public void saveAudit(Audit audit, String output) {
        audit.auditId = UUID.randomUUID().toString();
        synchronized (audits) {
            audits.add(audit);
            if (audits.size() > 1000) {
                audits.remove(0);
            }
        }
    }

    @Override
    public List<Audit> getAudit(String serviceId, int year, int month, int startDay, int endDay) {
        List<Audit> temp = new LinkedList<>();
        for (Audit audit : audits) {
            if (audit.serviceId.equals(serviceId)) {
                if (audit.auditId == null) {
                    audit.auditId = UUID.randomUUID().toString();
                }
                temp.add(audit);
            }
        }
        return temp;
    }

    @Override
    public String getAuditOutput(String serviceId, String auditId) {
        return null;
    }

    @Override
    public void updateSatus(String id, boolean busy, String status) {
    }

}
