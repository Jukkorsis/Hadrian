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
package com.northernwall.hadrian;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.parameters.PropertiesParameters;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.LoggerFactory;

/**
 * The Main class is intended to do four things 1) get the parameters that
 * Hadrian will use, 2) start the logging sub-system, 3) build an instance of
 * Hadrian, and lastly 4) start Hadrian.
 *
 * You do not need to use this Main class to start Hadrian. You can write you
 * our class that does these four steps.
 *
 * @author Richard Thurston
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Parameters parameters = loadParameters(args);

            startLogging(parameters);

            HadrianBuilder.create(parameters)
                    .builder()
                    .start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Parameters loadParameters(String[] args) {
        String filename;
        Properties properties = new Properties();
        if (args == null || args.length == 0) {
            System.out.println("Missing command line argument properties filename, using hadrian.properties");
            filename = Const.PROPERTIES_FILENAME;
        } else {
            filename = args[0];
        }
        try {
            properties.load(new FileInputStream(filename));
        } catch (IOException ex) {
            System.out.println("Can not load properties from " + filename + ", using defaults");
            properties = new Properties();
        }
        return new PropertiesParameters(properties);
    }

    private static void startLogging(Parameters parameters) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        context.reset();

        try {
            String config = parameters.getString(Const.LOGBACK_CONFIG, null);
            if (config != null && !config.isEmpty()) {
                System.out.println("Loading logback config from parameter value");
                configurator.doConfigure(config);
                StatusPrinter.printInCaseOfErrorsOrWarnings(context);
                return;
            }

            String filename = parameters.getString(Const.LOGBACK_FILENAME, Const.LOGBACK_FILENAME_DEFAULT);
            File file = new File(filename);
            if (file.exists()) {
                System.out.println("Loading logback config from file, " + filename);
                configurator.doConfigure(file);
                StatusPrinter.printInCaseOfErrorsOrWarnings(context);
                return;
            }

            System.out.println("Can not load logback config from parameter value or file, using defaults");
            configurator.doConfigure(Main.class.getResourceAsStream("/" + Const.LOGBACK_FILENAME_DEFAULT));
        } catch (JoranException je) {
            System.out.println("Could not find/load logback config file, exiting");
            System.out.println("Joran exception is " + je.getMessage());
            System.exit(0);
        }
    }

}
