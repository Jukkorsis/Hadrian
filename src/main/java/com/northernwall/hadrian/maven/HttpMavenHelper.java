package com.northernwall.hadrian.maven;

import com.northernwall.hadrian.Const;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpMavenHelper extends MavenHelper {
    private final static Logger logger = LoggerFactory.getLogger(HttpMavenHelper.class);

    private final OkHttpClient client;
    private final String mavenRepo;
    private final String mavenUsername;
    private final String mavenPassword;

    public HttpMavenHelper(Properties properties, OkHttpClient client) {
        super(properties);
        this.client = client;
        mavenRepo = Const.HTTP + properties.getProperty(Const.MAVEN_URL, Const.MAVEN_URL_DEFAULT);
        mavenUsername = properties.getProperty(Const.MAVEN_USERNAME, Const.MAVEN_USERNAME_DEFAULT);
        mavenPassword = properties.getProperty(Const.MAVEN_PASSWORD, Const.MAVEN_PASSWORD_DEFAULT);
    }
    
    @Override
    public List<String> readMavenVersions(String groupId, String artifactId) {
        if (groupId != null && artifactId != null) {
            try {
                Request.Builder builder = new Request.Builder();
                builder.url(mavenRepo + groupId.replace(".", "/") + "/" + artifactId + "/maven-metadata.xml");
                if (!mavenUsername.equals(Const.MAVEN_USERNAME_DEFAULT)) {
                    String credential = Credentials.basic(mavenUsername, mavenPassword);
                    builder.header("Authorization", credential);
                }
                Request request = builder.build();
                Response response = client.newCall(request).execute();

                InputStream inputStream = response.body().byteStream();
                return a(inputStream);
            } catch (Exception ex) {
                logger.error("Error reading maven version from {} {}, {}", groupId, artifactId, ex.getMessage());
            }
        }
        return new LinkedList<>();
    }

}
