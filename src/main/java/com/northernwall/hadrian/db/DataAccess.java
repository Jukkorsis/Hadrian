package com.northernwall.hadrian.db;

import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceHeader;
import com.northernwall.hadrian.domain.VersionHeader;
import java.util.List;


public interface DataAccess {

    Service getService(String id);

    List<ServiceHeader> getServiceHeaders();

    List<Service> getServices();

    List<VersionHeader> getVersions();

    void save(Service service);

    void update(Service service);
    
}
