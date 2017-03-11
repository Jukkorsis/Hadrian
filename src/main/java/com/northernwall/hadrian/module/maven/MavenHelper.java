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
package com.northernwall.hadrian.module.maven;

import com.northernwall.hadrian.config.Const;
import com.northernwall.hadrian.domain.Module;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.module.ModuleArtifactHelper;
import com.northernwall.hadrian.module.SematicVersionComparator;
import com.northernwall.hadrian.parameters.Parameters;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MavenHelper implements ModuleArtifactHelper {

    private final static Logger LOGGER = LoggerFactory.getLogger(MavenHelper.class);

    private final OkHttpClient client;
    protected final Parameters parameters;
    private final SematicVersionComparator mavenVersionComparator;

    public MavenHelper(Parameters parameters, OkHttpClient client) {
        this.parameters = parameters;
        this.mavenVersionComparator = new SematicVersionComparator();
        this.client = client;
    }

    @Override
    public List<String> readArtifactVersions(Service service, Module module, boolean includeSnapshots) {
        List<String> versions = new LinkedList<>();
        if (service.getMavenGroupId() != null
                && !service.getMavenGroupId().isEmpty()
                && module.getMavenArtifactId() != null
                && !module.getMavenArtifactId().isEmpty()) {
            try {
                Request.Builder builder = new Request.Builder();
                String mavenRepo = parameters.getString(Const.MAVEN_URL, Const.MAVEN_URL_DEFAULT);
                String url = mavenRepo
                        + service.getMavenGroupId().replace(".", "/")
                        + "/"
                        + module.getMavenArtifactId()
                        + "/maven-metadata.xml";
                builder.url(url);
                String mavenUsername = parameters.getString(Const.MAVEN_USERNAME, Const.MAVEN_USERNAME_DEFAULT);
                String mavenPassword = parameters.getString(Const.MAVEN_PASSWORD, Const.MAVEN_PASSWORD_DEFAULT);
                if (!mavenUsername.equals(Const.MAVEN_USERNAME_DEFAULT)) {
                    String credential = Credentials.basic(mavenUsername, mavenPassword);
                    builder.header("Authorization", credential);
                }
                Request request = builder.build();
                Response response = client.newCall(request).execute();

                try (InputStream inputStream = response.body().byteStream()) {
                    versions = processMavenStream(inputStream, includeSnapshots);
                }
            } catch (Exception ex) {
                LOGGER.error("Error reading maven version from {} {}, {}",
                        service.getMavenGroupId(),
                        module.getMavenArtifactId(),
                        ex.getMessage());
            }
        }

        return versions;
    }

    private List<String> processMavenStream(InputStream inputStream, boolean includeSnapshots) throws Exception {
        List<String> versions = new LinkedList<>();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputStream);
        Element root = doc.getDocumentElement();
        Node versionsNode = root.getElementsByTagName("versions").item(0);
        for (int i = 0; i < versionsNode.getChildNodes().getLength(); i++) {
            Node child = versionsNode.getChildNodes().item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (includeSnapshots || !child.getTextContent().endsWith(Const.MAVEN_SNAPSHOT)) {
                    versions.add(child.getTextContent());
                }
            }
        }
        Collections.sort(versions, mavenVersionComparator);
        int maxMavenVersions = parameters.getInt(Const.MAVEN_MAX_VERSIONS, Const.MAVEN_MAX_VERSIONS_DEFAULT);
        if (versions.size() > maxMavenVersions) {
            return versions.subList(0, maxMavenVersions);
        }
        return versions;
    }

}
