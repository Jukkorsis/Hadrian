package com.northernwall.hadrian.maven.ssh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.northernwall.hadrian.maven.MavenHelper;
import com.northernwall.hadrian.parameters.Parameters;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshMavenHelper extends MavenHelper {
    private final static Logger logger = LoggerFactory.getLogger(SshMavenHelper.class);
    
    private final String userName;
    private final String url;
    private final JSch jsch;
    
    public SshMavenHelper(Parameters parameters) {
        super(parameters);
        
        userName = parameters.getString("maven.ssh.userName", null);
        url = parameters.getString("maven.ssh.url", null);

        jsch = new JSch();
        String keyFile = parameters.getString("maven.ssh.keyFile", null);
        if (keyFile != null) {
            try {
                jsch.addIdentity(keyFile);
            } catch (JSchException ex) {
                throw new RuntimeException("failed to add private identity file", ex);
            }
        }
    }

    @Override
    public List<String> readMavenVersions(String groupId, String artifactId) {
        try {
            
            Session session = jsch.getSession(userName, url, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            
            // exec 'scp -f rfile' remotely
            String command = "scp -f " + "/mvnrepo/internal/" + groupId.replace(".", "/") + "/" + artifactId + "/maven-metadata.xml";
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            //OutputStream out = channel.getOutputStream();
            InputStream inputStream = channel.getInputStream();

            channel.connect();

            return processMavenStream(inputStream);
        } catch (Exception ex) {
            logger.error("doh", ex);
        }
        return new LinkedList<>();
    }

}
