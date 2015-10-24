package com.northernwall.hadrian;

public class Const {
    public static final String PROPERTIES_FILENAME = "hadrian.properties";
    
    public static final String LOGBACK_FILENAME = "logback.filename";
    public static final String LOGBACK_FILENAME_DEFAULT = "logback.xml";
    
    public static final String NO_STATUS = "-";
    
    public static final String HTTP = "http://";
    public static final String HOST = "{host}";

    public static final String JETTY_PORT = "jetty.port";
    public static final String JETTY_PORT_DEFAULT = "9090";
    public static final String JETTY_IDLE_TIMEOUT = "jetty.idleTimeout";
    public static final String JETTY_IDLE_TIMEOUT_DEFAULT = "1000";
    public static final String JETTY_ACCEPT_QUEUE_SIZE = "jetty.idleTimeout";
    public static final String JETTY_ACCEPT_QUEUE_SIZE_DEFAULT = "100";
    
    public static final String WEB_HOOK_CALLBACK_HOST = "webhook.callbackHost";
    public static final String WEB_HOOK_CALLBACK_HOST_DEFAULT = "127.0.0.1";
    public static final String WEB_HOOK_HOST_URL = "webhook.hostUrl";
    public static final String WEB_HOOK_HOST_URL_DEFAULT = "127.0.0.1:9090/webhook/host";
    public static final String WEB_HOOK_VIP_URL = "webhook.vipUrl";
    public static final String WEB_HOOK_VIP_URL_DEFAULT = "127.0.0.1:9090/webhook/vip";
    public static final String WEB_HOOK_HOST_VIP_URL = "webhook.hostVipUrl";
    public static final String WEB_HOOK_HOST_VIP_URL_DEFAULT = "127.0.0.1:9090/webhook/hostvip";

    public static final String MAVEN_URL = "maven.url";
    public static final String MAVEN_URL_DEFAULT = "127.0.0.1/mvnrepo/internal/";
    public static final String MAVEN_USERNAME = "maven.username";
    public static final String MAVEN_USERNAME_DEFAULT = "-";
    public static final String MAVEN_PASSWORD = "maven.password";
    public static final String MAVEN_PASSWORD_DEFAULT = "-";
    public static final String MAVEN_MAX_VERSIONS = "maven.maxVersions";
    public static final String MAVEN_MAX_VERSIONS_DEFAULT = "15";
    public static final String MAVEN_SNAPSHOT = "SNAPSHOT";

    public static final String DATA_ACCESS_FACTORY_CLASS_NAME = "dataAccess.factoryClassName";
    public static final String DATA_ACCESS_FACTORY_CLASS_NAME_DEFAULT = "com.northernwall.hadrian.db.InMemoryDataAccessFactory";

    private Const() {
    }
    
}
