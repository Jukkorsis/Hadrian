package com.northernwall.hadrian.maven.http;

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.maven.MavenHelper;
import com.northernwall.hadrian.parameters.Parameters;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpMavenHelper extends MavenHelper {
    private final static Logger logger = LoggerFactory.getLogger(HttpMavenHelper.class);

    private final OkHttpClient client;
    private final String mavenRepo;
    private final String mavenUsername;
    private final String mavenPassword;

    public HttpMavenHelper(Parameters parameters, OkHttpClient client) {
        super(parameters);
        this.client = client;
        mavenRepo = parameters.getString(Const.MAVEN_URL, Const.MAVEN_URL_DEFAULT);
        mavenUsername = parameters.getString(Const.MAVEN_USERNAME, Const.MAVEN_USERNAME_DEFAULT);
        mavenPassword = parameters.getString(Const.MAVEN_PASSWORD, Const.MAVEN_PASSWORD_DEFAULT);
    }
    
    @Override
    public List<String> readMavenVersions(String groupId, String artifactId) {
        List<String> versions = new LinkedList<>();
        if (groupId != null && !groupId.isEmpty() && artifactId != null && !artifactId.isEmpty()) {
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
                versions = processMavenStream(inputStream);
            } catch (Exception ex) {
                logger.error("Error reading maven version from {} {}, {}", groupId, artifactId, ex.getMessage());
            }
        }
        
        if (versions.isEmpty()) {
            versions.add("0.0.0");
        }
        return versions;
    }

}
