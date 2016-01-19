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
    
    public static final String OPERATION_CREATE = "create";
    public static final String OPERATION_DEPLOY = "deploy";
    public static final String OPERATION_UPDATE = "update";
    public static final String OPERATION_DELETE = "delete";
    
    public static final String TYPE_SERVICE = "service";
    public static final String TYPE_HOST = "host";
    public static final String TYPE_VIP = "vip";
    public static final String TYPE_HOST_VIP = "hostvip";
    
    public static final String ATTR_SESSION = "session";
    public static final String ATTR_USER = "user";

    public static final String TEXT = "text/plain; charset=utf-8";
    public static final String HTML = "text/html; charset=utf-8";
    public static final String JSON = "application/json; charset=utf-8";
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse(JSON);

    public static final String MAVEN_SNAPSHOT = "SNAPSHOT";
    
    public static final String GIT_PATH_URL = "gitPathUrl";
    public static final String GIT_PATH_URL_DETAULT = "git@github.com:{git}.git";
    public static final String GIT_PATH_URL_PATTERN = "{git}";

    //Properties file constants
    public static final String PROPERTIES_FILENAME = "hadrian.properties";

    public static final String LOGBACK_CONFIG = "logback.config";
    public static final String LOGBACK_FILENAME = "logback.filename";
    public static final String LOGBACK_FILENAME_DEFAULT = "logback.xml";
    
    public static final String JETTY_PORT = "jetty.port";
    public static final int JETTY_PORT_DEFAULT = 9090;
    public static final String JETTY_IDLE_TIMEOUT = "jetty.idleTimeout";
    public static final int JETTY_IDLE_TIMEOUT_DEFAULT = 1000;
    public static final String JETTY_ACCEPT_QUEUE_SIZE = "jetty.idleTimeout";
    public static final int JETTY_ACCEPT_QUEUE_SIZE_DEFAULT = 100;
    
    public static final String HOST_DETAILS_URL = "host.detailsUrl";
    public static final String HOST_DETAILS_ATTRIBUTES = "host.detailsAttrs";

    public static final String WORK_ITEM_SENDER_FACTORY_CLASS_NAME = "workItemSender.factoryClassName";
    public static final String WORK_ITEM_SENDER_FACTORY_CLASS_NAME_DEFAULT = "com.northernwall.hadrian.workItem.noop.NoopWorkItemSenderFactory";

    public static final String WORK_ITEM_STATUS_SUCCESS = "success";
    
    public static final String SIMPLE_WORK_ITEM_URL = "simpleWorkItem.url";
    public static final String SIMPLE_WORK_ITEM_URL_DEFAULT = "http://127.0.0.1:9090/webhook/simple";

    public static final String EMAIL_WORK_ITEM_SMTP_HOSTNAME = "emailWorkItem.smtp.hostname";
    public static final String EMAIL_WORK_ITEM_SMTP_POST = "emailWorkItem.smtp.port";
    public static final int EMAIL_WORK_ITEM_SMTP_POST_DEFAULT = 25; //465;
    public static final String EMAIL_WORK_ITEM_SMTP_SSL = "emailWorkItem.smtp.ssl";
    public static final boolean EMAIL_WORK_ITEM_SMTP_SSL_DEFAULT = false;
    public static final String EMAIL_WORK_ITEM_SMTP_USERNAME = "emailWorkItem.smtp.username";
    public static final String EMAIL_WORK_ITEM_SMTP_PASSWORD = "emailWorkItem.smtp.password";
    public static final String EMAIL_WORK_ITEM_EMAIL_TO = "emailWorkItem.emailTo";
    public static final String EMAIL_WORK_ITEM_EMAIL_From = "emailWorkItem.emailFrom";

    public static final String MAVEN_HELPER_FACTORY_CLASS_NAME = "maven.factoryClassName";
    public static final String MAVEN_HELPER_FACTORY_CLASS_NAME_DEFAULT = "com.northernwall.hadrian.maven.http.HttpMavenHelperFactory";

    public static final String MAVEN_MAX_VERSIONS = "maven.maxVersions";
    public static final int MAVEN_MAX_VERSIONS_DEFAULT = 15;

    public static final String MAVEN_URL = "maven.http.url";
    public static final String MAVEN_URL_DEFAULT = "http://127.0.0.1/mvnrepo/internal/";
    public static final String MAVEN_USERNAME = "maven.http.username";
    public static final String MAVEN_USERNAME_DEFAULT = "-";
    public static final String MAVEN_PASSWORD = "maven.http.password";
    public static final String MAVEN_PASSWORD_DEFAULT = "-";

    public static final String DATA_ACCESS_FACTORY_CLASS_NAME = "dataAccess.factoryClassName";
    public static final String DATA_ACCESS_FACTORY_CLASS_NAME_DEFAULT = "com.northernwall.hadrian.db.inMemory.InMemoryDataAccessFactory";
    //public static final String DATA_ACCESS_FACTORY_CLASS_NAME_DEFAULT = "com.northernwall.hadrian.db.cassandra.CassandraDataAccessFactory";

    public static final String CASS_NODE = "dataAccess.cassandra.node";
    public static final String CASS_NODE_DEFAULT = "127.0.0.1";
    public static final String CASS_KEY_SPACE = "dataAccess.cassandra.keyspace";
    public static final String CASS_KEY_SPACE_DEFAULT = "devops";
    public static final String CASS_REPLICATION_FACTOR = "dataAccess.cassandra.replicationFactor";
    public static final int CASS_REPLICATION_FACTOR_DEFAULT = 1;
    
    public static final String IN_MEMORY_DATA_FILE_NAME = "dataAccess.inMemory.dataFileName";
    public static final String IN_MEMORY_DATA_FILE_NAME_DEFAULT = "data.json";
    
    public static final String ACCESS_HANDLER_FACTORY_CLASS_NAME = "accessHandler.factoryClassName";
    public static final String ACCESS_HANDLER_FACTORY_CLASS_NAME_DEFAULT = "com.northernwall.hadrian.access.simple.SimpleAccessHandlerFactory";

    public static final String CONFIG_VERSION_URL = "config.versionUrl";
    public static final String CONFIG_VERSION_URL_DEFAULT = "{host}.mydomain.com:9090/version";
    public static final String CONFIG_AVAILABILITY_URL = "config.availabilityUrl";
    public static final String CONFIG_AVAILABILITY_URL_DEFAULT = "{host}.mydomain.com:9090/availability";
    public static final String CONFIG_START_CMD = "config.startCmd";
    public static final String CONFIG_START_CMD_DEFAULT = "screen -d -m java -jar serviceAbbr.jar";
    public static final String CONFIG_STOP_CMD = "config.stopCmd";
    public static final String CONFIG_STOP_CMD_DEFAULT = "pkill -f serviceAbbr.jar";

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
    public static final String CONFIG_ARTIFACT_TYPES_DEFAULT = "jar, war, targz";
    public static final String CONFIG_TEMPLATES = "config.templates";
    public static final String CONFIG_TEMPLATES_DEFAULT = "No template, Java template";
    
    private Const() {
    }
    
}
