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

package com.northernwall.hadrian.formData;

import com.northernwall.hadrian.domain.Action;
import com.northernwall.hadrian.domain.Link;
import com.northernwall.hadrian.domain.ListItem;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Richard Thurston
 */
public class ServiceFormData {

    public String _id;
    public String name;
    public String team;
    public String product;
    public String description;
    public String access;
    public String type;
    public String state;
    public String tech;
    public String versionUrl;
    public List<Link> links = new LinkedList<>();
    public List<ListItem> haRatings = new LinkedList<>();
    public List<ListItem> classRatings = new LinkedList<>();
    public boolean enableManage = false;
    public String script;
    public String mavenUrl;
    public List<Action> actions = new LinkedList<>();
    public String api;
    public String status;

}
