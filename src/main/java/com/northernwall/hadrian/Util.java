/*
 * Copyright 2015 Richard Thurston.
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

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {

    private final static Logger logger = LoggerFactory.getLogger(Util.class);

    private static final Gson gson = new Gson();

    public static final <T> T fromJson(org.eclipse.jetty.server.Request request, Class<T> classOfT) throws IOException {
        Reader reader = new InputStreamReader(request.getInputStream());
        T temp = gson.fromJson(reader, classOfT);
        logger.info("Stream->Json {}", gson.toJson(temp));
        return temp;
    }

    public static Date getGmt() {
        return Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
    }

    private Util() {
    }

}
