package com.northernwall.hadrian.handler;

import com.google.gson.Gson;
import com.northernwall.hadrian.db.DataAccess;
import java.io.BufferedWriter;
import java.io.IOException;
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
                    case "GET":
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

    private void manage(Request request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain;charset=utf-8");
        try (BufferedWriter writer = new BufferedWriter(new java.io.OutputStreamWriter(response.getOutputStream()))) {
            //Validation
            if (!request.getParameterMap().containsKey("app")) {
                writeLine(writer, "Error: Missing 'app' query parameter.");
                return;
            }
            if (request.getParameterMap().get("app").length > 1) {
                writeLine(writer, "Error: Only one 'app' query parameter alloweed.");
                return;
            }
            if (!request.getParameterMap().containsKey("host")) {
                writeLine(writer, "Error: Missing 'host' query parameter.");
                return;
            }
            if (!request.getParameterMap().containsKey("action")) {
                writeLine(writer, "Error: Missing 'action' query parameter.");
                return;
            }
            for (String action : request.getParameterMap().get("action")) {
                if (action.equals("deploy") || action.equals("link")) {
                    if (!request.getParameterMap().containsKey("version")) {
                        writeLine(writer, "Error: Missing 'version' query parameter. Version required for actions deploy or link.");
                        return;
                    }
                    if (request.getParameterMap().get("version").length > 1) {
                        writeLine(writer, "Error: Only one 'version' query parameter alloweed.");
                        return;
                    }
                }
            }
            //Execute
            writeLine(writer, "*** Starting " + request.getParameterMap().get("host").length + " hosts and " + request.getParameterMap().get("action").length + " actions ***");
            for (String host : request.getParameterMap().get("host")) {
                writeLine(writer, "*** Starting " + host + " ***");
                writeLine(writer, " ");
                writeLine(writer, " ");
                for (String action : request.getParameterMap().get("action")) {
                    String app = request.getParameterMap().get("app")[0];
                    String version = null;
                    if (request.getParameterMap().containsKey("version")) {
                        version = request.getParameterMap().get("version")[0];
                    }
                    execute(writer, app, host, action, version);
                }
                writeLine(writer, "*** Finished " + host + " ***");
            }
            writeLine(writer, "*** Finished all hosts ***");
        }
    }

    private void execute(BufferedWriter writer, String app, String host, String action, String version) throws IOException {
        String command = app + ".sh " + host + " " + action;
        if (version != null) {
            command = command + " " + version;
        }
        writeLine(writer, "*** Executing '" + command + "' ***");
        writeLine(writer, "    TODO: Actually execute the script and pipe back the results...");
        writeLine(writer, "*** Executed '" + command + "' ***");
        writeLine(writer, " ");
        writeLine(writer, " ");
    }

    private void writeLine(BufferedWriter writer, String text) throws IOException {
        text = text + System.lineSeparator();
        writer.write(text, 0, text.length());
    }

}
