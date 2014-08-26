package com.northernwall.hadrian.db;

import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceHeader;
import com.northernwall.hadrian.domain.ServiceRefView;
import com.northernwall.hadrian.domain.VersionView;
import java.util.List;


public interface DataAccess {

    Service getService(String id);

    List<ServiceHeader> getServiceHeaders();

    List<VersionView> getVersionVeiw();

    List<ServiceRefView> getServiceRefVeiw();

    void save(Service service);

    void update(Service service);
    
}
