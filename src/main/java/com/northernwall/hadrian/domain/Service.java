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

package com.northernwall.hadrian.domain;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Richard Thurston
 */
public class Service extends ServiceHeader {
    public static final String DEFAULT_IMAGE = "/ui/img/serviceLogo.png";

    public String tech;
    public List<Env> envs = new LinkedList<>();
    public List<Link> links = new LinkedList<>();
    public String versionUrl;
    public List<String> images;
    public List<ListItem> haRatings = new LinkedList<>();
    public List<ListItem> classRatings = new LinkedList<>();
    public List<Version> versions = new LinkedList<>();
    public List<Warning> warnings = new LinkedList<>();
    public List<Audit> audits = new LinkedList<>();

    public Version findVersion(String api) {
        if (versions == null || versions.isEmpty()) {
            return null;
        }
        for (Version version : versions) {
            if (version.api.equals(api)) {
                return version;
            }
        }
        return null;
    }

    public void addVersion(Version version) {
        if (versions == null) {
            versions = new LinkedList<>();
        }
        versions.add(version);
    }

    public Env findEnv(String name) {
        if (envs == null || envs.isEmpty()) {
            return null;
        }
        for (Env env : envs) {
            if (env.name.equals(name)) {
                return env;
            }
        }
        return null;
    }

    public void addEnv(Env env) {
        if (envs == null) {
            envs = new LinkedList<>();
        }
        envs.add(env);
    }

    public void addAudit(Audit audit) {
        if (audits == null) {
            audits = new LinkedList<>();
        }
        audits.add(audit);
    }

}
