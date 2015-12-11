package com.northernwall.hadrian;

import com.squareup.okhttp.MediaType;

public class Const {
    public static final String NO_STATUS = "-";
    
    public static final String HTTP = "http://";
    public static final String HTTP_GET = "GET";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_PUT = "PUT";
    public static final String HTTP_DELETE = "DELETE";
    public static final String COOKIE_SESSION = "session";
    public static final String COOKIE_PORTAL_NAME = "portalname";
    public static final int COOKIE_EXPRIY = 24*60*60*1000;
    public static final String HOST = "{host}";
    
    public static final String HOST_DETAILS_URL = "host.detailsUrl";
    public static final String HOST_DETAILS_ATTRIBUTES = "host.detailsAttrs";

    public static final String ATTR_SESSION = "session";
    public static final String ATTR_USER = "user";

    public static final String TEXT = "text/plain; charset=utf-8";
    public static final String HTML = "text/html; charset=utf-8";
    public static final String JSON = "application/json; charset=utf-8";
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse(JSON);

    public static final String MAVEN_SNAPSHOT = "SNAPSHOT";

    //Properties file constants
    public static final String PROPERTIES_FILENAME = "hadrian.properties";

    public static final String LOGBACK_FILENAME = "logback.filename";
    public static final String LOGBACK_FILENAME_DEFAULT = "logback.xml";
    
    public static final String JETTY_PORT = "jetty.port";
    public static final String JETTY_PORT_DEFAULT = "9090";
    public static final String JETTY_IDLE_TIMEOUT = "jetty.idleTimeout";
    public static final String JETTY_IDLE_TIMEOUT_DEFAULT = "1000";
    public static final String JETTY_ACCEPT_QUEUE_SIZE = "jetty.idleTimeout";
    public static final String JETTY_ACCEPT_QUEUE_SIZE_DEFAULT = "100";
    
    public static final String WEB_HOOK_CALLBACK_HOST = "webhook.callbackHost";
    public static final String WEB_HOOK_CALLBACK_HOST_DEFAULT = "http://127.0.0.1";
    public static final String WEB_HOOK_SERVICE_URL = "webhook.serviceUrl";
    public static final String WEB_HOOK_SERVICE_URL_DEFAULT = "http://127.0.0.1:9090/webhook/service";
    public static final String WEB_HOOK_HOST_URL = "webhook.hostUrl";
    public static final String WEB_HOOK_HOST_URL_DEFAULT = "http://127.0.0.1:9090/webhook/host";
    public static final String WEB_HOOK_VIP_URL = "webhook.vipUrl";
    public static final String WEB_HOOK_VIP_URL_DEFAULT = "http://127.0.0.1:9090/webhook/vip";
    public static final String WEB_HOOK_HOST_VIP_URL = "webhook.hostVipUrl";
    public static final String WEB_HOOK_HOST_VIP_URL_DEFAULT = "http://127.0.0.1:9090/webhook/hostvip";
    public static final String WEB_HOOK_DELAY = "webhook.delay";
    public static final String WEB_HOOK_DELAY_DEFAULT = "15";

    public static final String MAVEN_URL = "maven.url";
    public static final String MAVEN_URL_DEFAULT = "http://127.0.0.1/mvnrepo/internal/";
    public static final String MAVEN_USERNAME = "maven.username";
    public static final String MAVEN_USERNAME_DEFAULT = "-";
    public static final String MAVEN_PASSWORD = "maven.password";
    public static final String MAVEN_PASSWORD_DEFAULT = "-";
    public static final String MAVEN_MAX_VERSIONS = "maven.maxVersions";
    public static final String MAVEN_MAX_VERSIONS_DEFAULT = "15";

    public static final String MAVEN_HELPER_FACTORY_CLASS_NAME = "maven.factoryClassName";
    //public static final String MAVEN_HELPER_FACTORY_CLASS_NAME_DEFAULT = "com.northernwall.hadrian.maven.SshMavenHelperFactory";
    public static final String MAVEN_HELPER_FACTORY_CLASS_NAME_DEFAULT = "com.northernwall.hadrian.maven.HttpMavenHelperFactory";

    public static final String DATA_ACCESS_FACTORY_CLASS_NAME = "dataAccess.factoryClassName";
    public static final String DATA_ACCESS_FACTORY_CLASS_NAME_DEFAULT = "com.northernwall.hadrian.db.InMemoryDataAccessFactory";
    //public static final String DATA_ACCESS_FACTORY_CLASS_NAME_DEFAULT = "com.northernwall.hadrian.db.CassandraDataAccessFactory";

    public static final String ACCESS_FACTORY_CLASS_NAME = "access.factoryClassName";
    public static final String ACCESS_FACTORY_CLASS_NAME_DEFAULT = "com.northernwall.hadrian.access.SimpleAccessFactory";

    public static final String WEB_HOOK_SENDER_FACTORY_CLASS_NAME = "webHookSender.factoryClassName";
    public static final String WEB_HOOK_SENDER_FACTORY_CLASS_NAME_DEFAULT = "com.northernwall.hadrian.webhook.SimpleWebHookSenderFactory";

    public static final String CASS_NODE = "cassandra.node";
    public static final String CASS_NODE_DEFAULT = "127.0.0.1";
    public static final String CASS_KEY_SPACE = "cassandra.keyspace";
    public static final String CASS_KEY_SPACE_DEFAULT = "devops";
    public static final String CASS_REPLICATION_FACTOR = "cassandra.replicationFactor";
    public static final String CASS_REPLICATION_FACTOR_DEFAULT = "1";
    
    public static final String IN_MEMORY_DATA_FILE_NAME = "inMemory.dataFileName";
    public static final String IN_MEMORY_DATA_FILE_NAME_DEFAULT = "data.json";
    
    public static final String CONFIG_DATA_CENTERS = "config.dataCenters";
    public static final String CONFIG_DATA_CENTERS_DEFAULT = "dc";
    public static final String CONFIG_NETWORKS = "config.networks";
    public static final String CONFIG_NETWORKS_DEFAULT = "prd, tst";
    public static final String CONFIG_ENVSS = "config.envs";
    public static final String CONFIG_ENVS_DEFAULT = "Java7, Java8, NodeJS";
    public static final String CONFIG_SIZES = "config.sizes";
    public static final String CONFIG_SIZES_DEFAULT = "S, M, L, XL";
    public static final String CONFIG_PROTOCOLS = "config.protocols";
    public static final String CONFIG_PROTOCOLS_DEFAULT = "HTTP, HTTPS, TCP";
    public static final String CONFIG_DOMAINS = "config.domains";
    public static final String CONFIG_DOMAINS_DEFAULT = "northernwall.com";
    public static final String CONFIG_ARTIFACT_TYPES = "config.artifactTypes";
    public static final String CONFIG_ARTIFACT_TYPES_DEFAULT = ".jar, .war, .targz";
        
    private Const() {
    }
    
}
