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

package com.northernwall.hadrian.handler;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.WarningProcessor;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.Action;
import com.northernwall.hadrian.domain.ConfigItem;
import com.northernwall.hadrian.domain.Link;
import com.northernwall.hadrian.domain.ListItem;
import com.northernwall.hadrian.domain.PackageVersion;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceHeader;
import com.northernwall.hadrian.domain.Version;
import com.northernwall.hadrian.formData.ServiceFormData;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author Richard Thurston
 */
public class ServiceHandler extends SoaAbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(ServiceHandler.class);

    private final DataAccess dataAccess;
    private final WarningProcessor warningProcessor;
    private final CloseableHttpClient client;

    public ServiceHandler(DataAccess dataAccess, Gson gson, WarningProcessor warningProcessor, CloseableHttpClient client) {
        super(gson);
        this.dataAccess = dataAccess;
        this.warningProcessor = warningProcessor;
        this.client = client;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.equals("/services/services.json")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "GET":
                        listServices(response);
                        break;
                    case "POST":
                        createService(request);
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
            } else if (target.matches("/services/\\w+.json")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "GET":
                        getService(response, target.substring(10, target.length() - 5));
                        break;
                    case "POST":
                        updateService(request);
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
            } else if (target.matches("/services/\\w+/packageVersions.json")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "GET":
                        getPackageVersions(response, target.substring(10, target.length() - 21));
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void listServices(HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        List<ServiceHeader> services = dataAccess.getServiceHeaders();
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            jw.beginArray();
            for (ServiceHeader service : services) {
                gson.toJson(service, ServiceHeader.class, jw);
            }
            jw.endArray();
        }
    }

    private void getService(HttpServletResponse response, String id) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        Service service = dataAccess.getService(id);
        if (service == null) {
            throw new RuntimeException("Could not find service with id '" + id + "'");
        }
        for (ConfigItem haDimension : dataAccess.getConfig().haDimensions) {
            boolean found = false;
            for (ListItem haRating : service.haRatings) {
                if (haRating.name.equals(haDimension.code)) {
                    found = true;
                }
            }
            if (!found) {
                ListItem haRating = new ListItem();
                haRating.name = haDimension.code;
                haRating.level = haDimension.subItems.get(haDimension.subItems.size() - 1).code;
                service.haRatings.add(haRating);
            }
        }
        for (ConfigItem classDimension : dataAccess.getConfig().classDimensions) {
            boolean found = false;
            for (ListItem classRating : service.classRatings) {
                if (classRating.name.equals(classDimension.code)) {
                    found = true;
                }
            }
            if (!found) {
                ListItem classRating = new ListItem();
                classRating.name = classDimension.code;
                classRating.level = classDimension.subItems.get(classDimension.subItems.size() - 1).code;
                service.classRatings.add(classRating);
            }
        }
        service.images = new LinkedList<>();
        if (service.getAttachments() == null || service.getAttachments().isEmpty()) {
            service.images.add(Service.DEFAULT_IMAGE);
        } else {
            for (String name : service.getAttachments().keySet()) {
                String image = "/services/" + service.getId() + "/image/" + name;
                service.images.add(image);
                if (service.isImageLogoBlank()) {
                    service.imageLogo = image;
                }
            }
        }
        try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
            gson.toJson(service, Service.class, jw);
        }
    }

    private void createService(Request request) throws IOException {
        ServiceFormData serviceData = fromJson(request, ServiceFormData.class);
        if (!serviceData._id.matches("\\w+")) {
            logger.warn("New service {} contains an illegal character", serviceData._id);
            return;
        }
        Service cur = dataAccess.getService(serviceData._id);

        if (cur != null) {
            return;
        }
        Service service = new Service();
        service.setId(serviceData._id);
        service.date = System.currentTimeMillis();
        service.name = serviceData.name;
        service.team = serviceData.team;
        service.product = serviceData.product;
        service.description = serviceData.description;
        service.state = serviceData.state;
        service.access = serviceData.access;
        service.type = serviceData.type;
        service.tech = serviceData.tech;
        service.versionUrl = serviceData.versionUrl;
        service.imageLogo = Service.DEFAULT_IMAGE;
        Version version = new Version();
        version.api = serviceData.api;
        version.status = serviceData.status;
        service.versions = new LinkedList<>();
        service.versions.add(version);
        for (ConfigItem haDimension : dataAccess.getConfig().haDimensions) {
            ListItem haRating = new ListItem();
            haRating.name = haDimension.code;
            haRating.level = haDimension.subItems.get(haDimension.subItems.size() - 1).code;
            service.haRatings.add(haRating);
        }
        for (ConfigItem classDimension : dataAccess.getConfig().classDimensions) {
            ListItem classRating = new ListItem();
            classRating.name = classDimension.code;
            classRating.level = classDimension.subItems.get(classDimension.subItems.size() - 1).code;
            service.haRatings.add(classRating);
        }
        service.enableManage = serviceData.enableManage;
        service.mavenUrl = serviceData.mavenUrl;
        service.script = serviceData.script;
        dataAccess.save(service);
    }

    private void updateService(Request request) throws IOException {
        ServiceFormData serviceData = fromJson(request, ServiceFormData.class);
        Service cur = dataAccess.getService(serviceData._id);

        if (cur == null) {
            return;
        }
        cur.name = serviceData.name;
        cur.team = serviceData.team;
        cur.product = serviceData.product;
        cur.description = serviceData.description;
        cur.state = serviceData.state;
        cur.access = serviceData.access;
        cur.type = serviceData.type;
        cur.tech = serviceData.tech;
        cur.versionUrl = serviceData.versionUrl;
        cur.links = new LinkedList<>();
        for (Link link : serviceData.links) {
            if (link.name != null && !link.name.isEmpty() && link.url != null && !link.url.isEmpty()) {
                if (!link.url.toLowerCase().startsWith("http://") && !link.url.toLowerCase().startsWith("https://")) {
                    link.url = "http://" + link.url;
                }
                cur.links.add(link);
            }
        }
        Collections.sort(cur.links, new Comparator<Link>() {
            @Override
            public int compare(Link o1, Link o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        cur.haRatings = serviceData.haRatings;
        cur.classRatings = serviceData.classRatings;
        cur.enableManage = serviceData.enableManage;
        cur.script = serviceData.script;
        cur.mavenUrl = serviceData.mavenUrl;
        cur.actions = new LinkedList<>();
        for (Action action : serviceData.actions) {
            if (action.name != null && !action.name.isEmpty()) {
                cur.actions.add(action);
            }
        }
        dataAccess.save(cur);

        warningProcessor.scanServices();
    }

    private void getPackageVersions(HttpServletResponse response, String id) throws ParserConfigurationException, IOException, SAXException {
        Service cur = dataAccess.getService(id);
        if (cur == null) {
            return;
        }
        CloseableHttpResponse mavenResponse = null;
        try {
            HttpGet request = new HttpGet(cur.mavenUrl);
            mavenResponse = client.execute(request);
            if (mavenResponse.getStatusLine().getStatusCode() >= 300) {
                return;
            }
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(mavenResponse.getEntity().getContent());
            Element root = doc.getDocumentElement();
            Node versionsNode = root.getElementsByTagName("versions").item(0);
            List<PackageVersion> packageVersions = new LinkedList<>();
            for (int i = 0; i < versionsNode.getChildNodes().getLength(); i++) {
                Node child = versionsNode.getChildNodes().item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    PackageVersion packageVersion = new PackageVersion();
                    packageVersion.name = child.getTextContent();
                    packageVersions.add(packageVersion);
                }
            }
            Collections.sort(packageVersions, new Comparator<PackageVersion>() {
                @Override
                public int compare(PackageVersion o1, PackageVersion o2) {
                    return o2.name.compareTo(o1.name);
                }
            });
            try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream()))) {
                jw.beginArray();
                for (PackageVersion packageVersion : packageVersions) {
                    gson.toJson(packageVersion, PackageVersion.class, jw);
                }
                jw.endArray();
            }
        } finally {
            if (mavenResponse != null) {
                try {
                    mavenResponse.close();
                } catch (IOException ex) {
                    logger.error("Could not close http version connection");
                }
            }
        }
    }

}
