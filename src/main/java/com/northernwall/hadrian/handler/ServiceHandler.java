package com.northernwall.hadrian.handler;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.northernwall.hadrian.WarningProcessor;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.ConfigItem;
import com.northernwall.hadrian.domain.Env;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Link;
import com.northernwall.hadrian.domain.ListItem;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceHeader;
import com.northernwall.hadrian.domain.Version;
import com.northernwall.hadrian.formData.ServiceFormData;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceHandler extends SoaAbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(ServiceHandler.class);

    private final DataAccess dataAccess;
    private final WarningProcessor warningProcessor;
    private final CloseableHttpClient client;

    public ServiceHandler(DataAccess dataAccess, Gson gson, WarningProcessor warningProcessor, Properties properties) {
        super(gson);
        this.dataAccess = dataAccess;
        this.warningProcessor = warningProcessor;
        try {
            int maxConnections = Integer.parseInt(properties.getProperty("maxConnections", "100"));
            int maxPerRoute = Integer.parseInt(properties.getProperty("maxPerRoute", "10"));
            int socketTimeout = Integer.parseInt(properties.getProperty("socketTimeout", "1000"));
            int connectionTimeout = Integer.parseInt(properties.getProperty("connectionTimeout", "1000"));

            RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create();
            Registry<ConnectionSocketFactory> registry = registryBuilder.register("http", PlainConnectionSocketFactory.INSTANCE).build();

            PoolingHttpClientConnectionManager ccm = new PoolingHttpClientConnectionManager(registry);
            ccm.setMaxTotal(maxConnections);
            ccm.setDefaultMaxPerRoute(maxPerRoute);

            HttpClientBuilder clientBuilder = HttpClients.custom()
                    .setConnectionManager(ccm)
                    .setDefaultConnectionConfig(ConnectionConfig.custom()
                            .setCharset(Consts.UTF_8).build())
                    .setDefaultRequestConfig(RequestConfig.custom()
                            .setSocketTimeout(socketTimeout)
                            .setConnectTimeout(connectionTimeout).build());
            client = clientBuilder.build();
        } catch (NumberFormatException nfe) {
            throw new IllegalStateException("Error Creating HTTPClient, could not parse property");
        } catch (Exception e) {
            throw new IllegalStateException("Error Creating HTTPClient: ", e);
        }
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
        for (Env env : service.envs) {
            for (Host host : env.hosts) {
                host.implVersion = urlGet(host.name, host.port, service.versionUrl);
            }
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

    private String urlGet(String name, int port, String versionUrl) {
        CloseableHttpResponse response = null;
        try {
            HttpHost host = new HttpHost(name, port);
            HttpGet request = new HttpGet(versionUrl);
            response = client.execute(host, request);
            if (response.getStatusLine().getStatusCode() >= 300) {
                return "E" + response.getStatusLine().getStatusCode();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String content = reader.readLine();
            if (content == null || content.isEmpty()) {
                return "NULL";
            }
            if (content.length() > 12) {
                return content.substring(0, 12);
            }
            return content;
        } catch (IOException ex) {
            return "Unknown";
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ex) {
                    logger.error("Could not close http version connection");
                }
            }
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
        service.busValue = serviceData.busValue;
        service.pii = serviceData.pii;
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
        cur.busValue = serviceData.busValue;
        cur.pii = serviceData.pii;
        cur.links = new LinkedList<>();
        for (Link link : serviceData.links) {
            if (link.name != null && !link.name.isEmpty() && link.url != null && !link.url.isEmpty()) {
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
        cur.versionUrl = serviceData.versionUrl;
        dataAccess.save(cur);

        warningProcessor.scanServices();
    }

}
