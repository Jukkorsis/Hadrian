/*
 * Copyright 2016 Richard Thurston.
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
package com.northernwall.hadrian.messaging;

import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.parameters.Parameters;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author rthursto
 */
public abstract class MessageProcessor {

    public abstract void init(Parameters parameters);
    
    public abstract void process(MessageType messageType, Team team, Map<String, String> data);
    
    protected String replaceTerms(String pattern, Map<String, String> data) {
        if (pattern == null || pattern.isEmpty()) {
            return "";
        }
        for (Entry<String, String> entry : data.entrySet()) {
            String target = "{"+entry.getKey()+"}";
            if (pattern.contains(target)) {
                pattern = pattern.replace(target, entry.getValue());
            }
        }
        return pattern;
    }
}
