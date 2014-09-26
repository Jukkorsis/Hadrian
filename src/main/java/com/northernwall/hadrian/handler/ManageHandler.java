package com.northernwall.hadrian.handler;

import com.google.gson.Gson;
import com.northernwall.hadrian.db.DataAccess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        Map<String, String> data = parseFormDate(reader.readLine());

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

    private Map<String, String> parseFormDate(String input) {
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

    private void execute(BufferedWriter writer, String app, String env, String username, String password, String version, String host, String actions) throws IOException {
        String command = app + ".sh -u " + username + " -p " + password + " -e " + env + " -h " + host + " -a " + actions;
        String safeCommand = app + ".sh -u " + username + " -p **** -e " + env + " -h " + host + " -a " + actions;
        if (version != null) {
            command = command + " -v " + version;
            safeCommand = safeCommand + " -v " + version;
        }
        writeLine(writer, "*** Executing '" + safeCommand + "' ***");
        writeLine(writer, " ");
        writeLine(writer, "    TODO: Actually execute the script and pipe back the results...");
        writeLine(writer, " ");
        writeLine(writer, "*** Executed '" + safeCommand + "' ***");
        writeLine(writer, " ");
    }

    private void writeLine(BufferedWriter writer, String text) throws IOException {
        text = text + System.lineSeparator();
        writer.write(text, 0, text.length());
    }

}
