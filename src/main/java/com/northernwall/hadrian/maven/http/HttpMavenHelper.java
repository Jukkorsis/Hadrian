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
    private String mavenRepo;
    private String mavenUsername;
    private String mavenPassword;

    public HttpMavenHelper(Parameters parameters, OkHttpClient client) {
        super(parameters);
        this.client = client;
    }

    @Override
    public void setup() {
        super.setup();
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
                String url = mavenRepo + groupId.replace(".", "/") + "/" + artifactId + "/maven-metadata.xml"; 
                builder.url(url);
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
