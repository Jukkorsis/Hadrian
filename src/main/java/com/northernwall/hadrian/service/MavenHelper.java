package com.northernwall.hadrian.service;

import com.northernwall.hadrian.Const;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class MavenHelper {
    private final static Logger logger = LoggerFactory.getLogger(MavenHelper.class);
    
    private final String mavenRepo;
    private final int maxMavenVersions;
    private final OkHttpClient client;
    private final String mavenUsername;
    private final String mavenPassword;

    public MavenHelper(Properties properties, OkHttpClient client) {
        this.client = client;
        mavenRepo = Const.HTTP + properties.getProperty(Const.MAVEN_URL, Const.MAVEN_URL_DEFAULT);
        maxMavenVersions = Integer.parseInt(properties.getProperty(Const.MAVEN_MAX_VERSIONS, Const.MAVEN_MAX_VERSIONS_DEFAULT));
        mavenUsername = properties.getProperty(Const.MAVEN_USERNAME, Const.MAVEN_USERNAME_DEFAULT);
        mavenPassword = properties.getProperty(Const.MAVEN_PASSWORD, Const.MAVEN_PASSWORD_DEFAULT);
    }

    public List<String> readMavenVersions(String groupId, String artifactId) {
        List<String> versions = new LinkedList<>();
        if (groupId != null && artifactId != null) {
            try {
                Builder builder = new Request.Builder();
                builder.url(mavenRepo + groupId.replace(".", "/") + "/" + artifactId + "/maven-metadata.xml");
                if (!mavenUsername.equals(Const.MAVEN_USERNAME_DEFAULT)) {
                    String credential = Credentials.basic(mavenUsername, mavenPassword);
                    builder.header("Authorization", credential);
                }
                Request request = builder.build();
                Response response = client.newCall(request).execute();

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(response.body().byteStream());
                Element root = doc.getDocumentElement();
                Node versionsNode = root.getElementsByTagName("versions").item(0);
                for (int i = 0; i < versionsNode.getChildNodes().getLength(); i++) {
                    Node child = versionsNode.getChildNodes().item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE && child.getTextContent().endsWith(Const.MAVEN_SNAPSHOT)) {
                        versions.add(child.getTextContent());
                    }
                }
            } catch (IOException | ParserConfigurationException | SAXException ex) {
                logger.error("Error reading maven version from {} {}, {}", groupId, artifactId, ex.getMessage());
                versions.clear();
            }
            Collections.reverse(versions);
            if (versions.size() > maxMavenVersions) {
                return versions.subList(0, maxMavenVersions);
            }
        }
        //todo, should remove and replace with a free form edit box
        if (versions.isEmpty()) {
            versions.add("4.0");
            versions.add("3.0");
            versions.add("2.0");
            versions.add("1.0");
        }
        return versions;
    }

}
