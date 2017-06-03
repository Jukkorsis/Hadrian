/*
 * Copyright 2017 Richard Thurston.
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
package com.northernwall.hadrian.sshAccess.simple;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.northernwall.hadrian.parameters.Parameters;
import com.northernwall.hadrian.sshAccess.SshAccess;
import com.northernwall.hadrian.sshAccess.SshEntry;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Richard
 */
public class SimpleSshAccess implements SshAccess {

    private final Parameters parameters;
    private final Gson gson;

    public SimpleSshAccess(Parameters parameters, Gson gson) {
        this.parameters = parameters;
        this.gson = gson;
    }

    @Override
    public List<SshEntry> getSshEntries() {
        String temp = parameters.getString("sshEntries", null);
        if (temp == null || temp.isEmpty()) {
            return new LinkedList<>();
        }
        Type listType = new TypeToken<ArrayList<SshEntry>>() {
        }.getType();
        return gson.fromJson(temp, listType);
    }

}
