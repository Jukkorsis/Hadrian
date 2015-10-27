package com.northernwall.hadrian.maven;

import com.northernwall.hadrian.Const;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class MavenHelper {
    private final static Logger logger = LoggerFactory.getLogger(MavenHelper.class);
    
    private final int maxMavenVersions;

    public MavenHelper(Properties properties) {
        this.maxMavenVersions = Integer.parseInt(properties.getProperty(Const.MAVEN_MAX_VERSIONS, Const.MAVEN_MAX_VERSIONS_DEFAULT));
    }

    public abstract List<String> readMavenVersions(String groupId, String artifactId);

    protected List<String> a(InputStream inputStream) throws Exception {
        List<String> versions = new LinkedList<>();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputStream);
        Element root = doc.getDocumentElement();
        Node versionsNode = root.getElementsByTagName("versions").item(0);
        for (int i = 0; i < versionsNode.getChildNodes().getLength(); i++) {
            Node child = versionsNode.getChildNodes().item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && !child.getTextContent().endsWith(Const.MAVEN_SNAPSHOT)) {
                versions.add(child.getTextContent());
            }
        }
        Collections.reverse(versions);
        if (versions.size() > maxMavenVersions) {
            return versions.subList(0, maxMavenVersions);
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
