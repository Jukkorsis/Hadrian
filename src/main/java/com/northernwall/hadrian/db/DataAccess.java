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

import com.northernwall.hadrian.domain.Config;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceHeader;
import com.northernwall.hadrian.domain.ServiceRefView;
import com.northernwall.hadrian.domain.VersionView;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 *
 * @author Richard Thurston
 */
public interface DataAccess {
    
    Config getConfig();
    
    void save(Config config);

    Service getService(String id);

    List<ServiceHeader> getServiceHeaders();

    List<VersionView> getVersionVeiw();

    List<ServiceRefView> getServiceRefVeiw();

    void save(Service service);

    public void uploadImage(String serviceId, String name, String contentType, InputStream openStream);
    
    public InputStream downloadImage(String serviceId, String name) throws IOException;
    
}
