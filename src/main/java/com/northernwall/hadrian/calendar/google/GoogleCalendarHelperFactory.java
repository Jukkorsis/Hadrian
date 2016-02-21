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
package com.northernwall.hadrian.calendar.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.Calendar.Builder;
import com.google.api.services.calendar.CalendarScopes;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.calendar.CalendarHelper;
import com.northernwall.hadrian.calendar.CalendarHelperFactory;
import com.northernwall.hadrian.parameters.Parameters;
import com.squareup.okhttp.OkHttpClient;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleCalendarHelperFactory implements CalendarHelperFactory {

    private final static Logger logger = LoggerFactory.getLogger(GoogleCalendarHelperFactory.class);
    private static final String APPLICATION_NAME = "";

    @Override
    public CalendarHelper create(Parameters parameters, OkHttpClient client) {
        try {
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            File dataStoreDir = new File(parameters.getString(Const.CALENDAR_GOOGLE_DATA_STORE_DIR, null));
            FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(dataStoreDir);
            // load client secrets
            Reader reader = new StringReader(parameters.getString(Const.CALENDAR_GOOGLE_CLIENT_SECRETS, null));
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, reader);
            // set up authorization code flow
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, Collections.singleton(CalendarScopes.CALENDAR))
                    .setDataStoreFactory(dataStoreFactory)
                    .build();
            Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
            //build calendar client
            Calendar calendarClient = new Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            logger.info("Finished building GoogleCalendarHelper successfully");
            return new GoogleCalendarHelper(calendarClient, parameters);
        } catch (IOException ex) {
            throw new RuntimeException("IO Exception while building GoogleCalendarHelper", ex);
        } catch (GeneralSecurityException ex) {
            throw new RuntimeException("General Security Exception while building GoogleCalendarHelper", ex);
        }
    }

}
