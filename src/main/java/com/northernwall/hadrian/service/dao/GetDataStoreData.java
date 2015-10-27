package com.northernwall.hadrian.service.dao;

import com.northernwall.hadrian.domain.DataStore;

public class GetDataStoreData {
    public String dataStoreId;
    public String name;
    public String serviceId;
    public String type;
    public String network;
    
    public static GetDataStoreData create(DataStore dataStore) {
        GetDataStoreData temp = new GetDataStoreData();
        temp.dataStoreId = dataStore.getDataStoreId();
        temp.name = dataStore.getName();
        temp.serviceId = dataStore.getServiceId();
        temp.type = dataStore.getType();
        temp.network = dataStore.getNetwork();
        return temp;
    }

}
