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

import com.codahale.metrics.MetricRegistry;
import com.northernwall.hadrian.parameters.Parameters;
import org.eclipse.jetty.server.Handler;

/**
 * The AccessHandlerFactory returns a Jetty Handler that validates user
 * logins/sessions. If a user is not logged in then the handler should redirect
 * the user to a log in page. If the user is logged in then the handler is
 * required to add the User to the request.
 * request.setAttribute(Const.ATTR_USER, user);
 *
 * @author rthursto
 */
public interface AccessHandlerFactory {
    Handler create(AccessHelper accessHelper, Parameters parameters, MetricRegistry metricRegistry);

}
