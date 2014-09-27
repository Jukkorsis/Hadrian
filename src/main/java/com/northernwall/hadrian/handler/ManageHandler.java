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
package com.northernwall.hadrian.handler;

import com.google.gson.Gson;
import com.northernwall.hadrian.db.DataAccess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.xfer.InMemorySourceFile;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Thurston
 */
public class ManageHandler extends SoaAbstractHandler {

    private final static Logger logger = LoggerFactory.getLogger(ManageHandler.class);

    private final DataAccess dataAccess;

    public ManageHandler(DataAccess dataAccess, Gson gson) {
        super(gson);
        this.dataAccess = dataAccess;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (target.matches("/manage")) {
                logger.info("Handling {} request {}", request.getMethod(), target);
                switch (request.getMethod()) {
                    case "POST":
                        manage(request, response);
                        break;
                }
                response.setStatus(200);
                request.setHandled(true);
            }
        } catch (Exception e) {
            logger.error("Exception {} while handling request for {}", e.getMessage(), target, e);
            response.setStatus(400);
        }
    }

    private void manage(Request request, HttpServletResponse response) throws IOException, ServletException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        Map<String, String> data = parseFormData(reader.readLine());

        response.setContentType("text/plain;charset=utf-8");
        try (BufferedWriter writer = new BufferedWriter(new java.io.OutputStreamWriter(response.getOutputStream()))) {
            //Validation parameters
            if (!data.containsKey("app")) {
                writeLine(writer, "Error: Missing 'app' query parameter.");
                return;
            }
            if (!data.containsKey("username") || data.get("username").equals("undefined")) {
                writeLine(writer, "Error: Missing 'username' query parameter.");
                return;
            }
            if (!data.containsKey("password") || data.get("password").equals("undefined")) {
                writeLine(writer, "Error: Missing 'password' query parameter.");
                return;
            }
            if (!data.containsKey("hosts")) {
                writeLine(writer, "Error: Missing 'hosts' query parameter.");
                return;
            }
            if (!data.containsKey("actions")) {
                writeLine(writer, "Error: Missing 'actions' query parameter.");
                return;
            }
            if (data.get("actions").contains("deploy") || data.get("actions").contains("link")) {
                if (!data.containsKey("version") || data.get("version").equals("undefined")) {
                    writeLine(writer, "Error: Missing 'version' query parameter. Version required for actions deploy or link.");
                    return;
                }
            }
            //Grab parameters
            String app = data.get("app");
            String env = data.get("env");
            String username = data.get("username");
            String password = data.get("password");
            String version = data.get("version");
            if (version.equals("undefined")) {
                version = null;
            }
            String actions = data.get("actions");
            if (actions.endsWith(",")) {
                actions = actions.substring(0, actions.length() - 1);
            }
            String[] hosts = data.get("hosts").split(",");
            //Execute
            writeLine(writer, "*** Performing " + actions + " on " + hosts.length + " hosts ***");
            writeLine(writer, " ");
            for (String host : hosts) {
                if (host != null && !host.isEmpty()) {
                    writeLine(writer, "*** Starting " + host + " ***");
                    writeLine(writer, " ");
                    execute(writer, app, env, username, password, version, host, actions);
                    writeLine(writer, "*** Finished " + host + " ***");
                    writeLine(writer, " ");
                }
            }
            writeLine(writer, "*** Finished all hosts ***");
        }
    }

    private Map<String, String> parseFormData(String input) {
        Map<String, String> data = new HashMap<>();
        input = input.replaceAll("%2C", ",");
        int i = input.indexOf("=");
        while (i > 0) {
            String k = input.substring(0, i);
            input = input.substring(i + 1);
            int ii = input.indexOf("&");
            if (ii >= 0) {
                String v = input.substring(0, ii);
                data.put(k, v);
                input = input.substring(ii + 1);
                i = input.indexOf("=");
            } else {
                data.put(k, input);
                i = -1;
            }
        }
        return data;
    }

    private void execute(BufferedWriter writer, String app, String env, String username, String password, String version, String host, String actions) {
        String commandText = app + ".sh -e " + env + " -a " + actions;
        if (version != null) {
            commandText = commandText + " -v " + version;
        }

        commandText = "ls -l";

        final SSHClient ssh = new SSHClient();
        try {
            try {
                ssh.loadKnownHosts();
                ssh.connect(host);
                ssh.authPassword(username, password);
            } catch (UserAuthException uae) {
                writeLine(writer, "!!! User Authentication error, " + uae.getMessage() + " !!!");
                writeLine(writer, " ");
                return;
            } catch (IOException ioe) {
                logger.warn("{} {} while connecting to {}", ioe.getClass().getSimpleName(), ioe.getMessage(), host);
                writeLine(writer, "!!! System error occured while establishing a connection to " + host + ", " + ioe.getMessage() + " !!!");
                writeLine(writer, " ");
                return;
            }
            writeLine(writer, "*** Transfering Script to execute ***");
            try {
                ssh.newSCPFileTransfer().upload(new ScriptSourceFile(app, dataAccess), host);
            } catch (IOException ioe) {
                logger.warn("{} {} while SCPing file to {}", ioe.getClass().getSimpleName(), ioe.getMessage(), host);
                writeLine(writer, "!!! System error occured while transfering script to " + host + ", " + ioe.getMessage() + " !!!");
                writeLine(writer, " ");
                return;
            }
            writeLine(writer, "*** Transfer complete ***");
            writeLine(writer, " ");
            writeLine(writer, "*** Executing '" + commandText + "' ***");
            writeLine(writer, " ");
            Session session = null;
            try {
                session = ssh.startSession();
                final Command cmd = session.exec(commandText);
                writeLine(writer, cmd.getInputStream());
                cmd.join(5, TimeUnit.SECONDS);
                writeLine(writer, " ");
                writeLine(writer, "*** Executed '" + commandText + "' exit status '" + cmd.getExitStatus() + "' ***");
                writeLine(writer, " ");
            } catch (TransportException  | ConnectionException ex) {
                logger.warn("{} {} while executing command on {}", ex.getClass().getSimpleName(), ex.getMessage(), host);
                writeLine(writer, "!!! System error occured while executing script on " + host + ", " + ex.getMessage() + " !!!");
                writeLine(writer, " ");
            } finally {
                if (session != null) {
                    try {
                        session.close();
                    } catch (TransportException | ConnectionException ex) {
                        logger.warn("{} {} while closing session to {}", ex.getClass().getSimpleName(), ex.getMessage(), host);
                    }
                }
            }
        } finally {
            try {
                ssh.disconnect();
            } catch (IOException ex) {
                logger.warn("{} {} while disconnecting from {}", ex.getClass().getSimpleName(), ex.getMessage(), host);
            }
        }

    }

    private void writeLine(BufferedWriter writer, InputStream stream) {
        String text = null;
        try {
            text = IOUtils.readFully(stream).toString();
        } catch (IOException ex) {
            logger.error("Exception reading command's response", ex);
        }
        if (text != null) {
            writeLine(writer, text);
        }
    }

    private void writeLine(BufferedWriter writer, String text) {
        text = text + System.lineSeparator();
        try {
            writer.write(text, 0, text.length());
        } catch (IOException ex) {
            logger.error("Exception while writing http response stream", ex);
        }
    }

}
