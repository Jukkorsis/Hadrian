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

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.Calendar.Builder;
import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.calendar.CalendarHelper;
import com.northernwall.hadrian.calendar.CalendarHelperFactory;
import com.northernwall.hadrian.calendar.simple.SimpleCalendarHelper;
import com.northernwall.hadrian.parameters.Parameters;
import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleCalendarHelperFactory implements CalendarHelperFactory {

    private final static Logger logger = LoggerFactory.getLogger(GoogleCalendarHelperFactory.class);

    @Override
    public CalendarHelper create(Parameters parameters, OkHttpClient client) {
        try {
            String appName = parameters.getString(Const.CALENDAR_GOOGLE_APP_NAME, "service-delivery-tool");
            String accountId = parameters.getString(Const.CALENDAR_GOOGLE_ACCOUNT_ID, null);
            String privateKeyId = parameters.getString(Const.CALENDAR_GOOGLE_PRIVATE_KEY_ID, null);
            PrivateKey privateKey = getPemPrivateKey(parameters);

            if (accountId == null || accountId.isEmpty()) {
                logger.error("{} can not be null or empty", Const.CALENDAR_GOOGLE_ACCOUNT_ID);
                return new SimpleCalendarHelper();
            }
            if (privateKeyId == null || privateKeyId.isEmpty()) {
                logger.error("{} can not be null or empty", Const.CALENDAR_GOOGLE_PRIVATE_KEY_ID);
                return new SimpleCalendarHelper();
            }
            if (privateKey == null) {
                logger.error("{} can not be null or empty", Const.CALENDAR_GOOGLE_PEM_FILE);
                return new SimpleCalendarHelper();
            }

            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            // load client secrets
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setServiceAccountId(accountId)
                    .setServiceAccountPrivateKeyId(privateKeyId)
                    .setServiceAccountPrivateKey(privateKey)
                    .setServiceAccountScopes(Collections.singleton("https://www.googleapis.com/auth/calendar"))
                    .build();

            //build calendar client
            Calendar calendarClient = new Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName(appName)
                    .build();
            logger.info("Finished building GoogleCalendarHelper successfully");
            return new GoogleCalendarHelper(calendarClient, parameters);
        } catch (IOException ex) {
            logger.error("IO Exception while building GoogleCalendarHelper", ex);
            return new SimpleCalendarHelper();
        } catch (GeneralSecurityException ex) {
            logger.error("General Security Exception while building GoogleCalendarHelper", ex);
            return new SimpleCalendarHelper();
        }
    }

    public PrivateKey getPemPrivateKey(Parameters parameters) throws GeneralSecurityException {
        String temp = parameters.getString(Const.CALENDAR_GOOGLE_PEM_FILE, null);
        if (temp == null || temp.isEmpty()) {
            return null;
        }
        byte[] decoded = Base64.getDecoder().decode(temp);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

}
