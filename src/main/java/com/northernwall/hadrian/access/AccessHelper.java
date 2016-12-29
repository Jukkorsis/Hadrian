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
package com.northernwall.hadrian.access;

import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.User;
import org.eclipse.jetty.server.Request;

public interface AccessHelper {

    User getUser(String username);
    boolean canUserModify(Request request, Team team);
    User checkIfUserCanModify(Request request, Team team, String action);
    User checkIfUserCanDeploy(Request request, Team team);
    User checkIfUserCanRestart(Request request, Team team);
    User checkIfUserCanAudit(Request request, Team team);
    boolean isAdmin(Request request, String action);
    User checkIfUserIsAdmin(Request request, String action);

}
