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
package com.northernwall.hadrian;

import com.northernwall.hadrian.handlers.caching.CachedContent;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Richard
 */
public class JsCompressionTest {

    public JsCompressionTest() {
    }

    @Test
    public void compressionTest() {
        String orig = "var hadrianApp = angular.module('HadrianApp', [\n"
                + "    'ngRoute',\n"
                + "    'ngAnimate',    'ui.bootstrap',\n"
                + "    'hadrianDirectives',\n"
                + "    'hadrianControllers',\n"
                + "    'hadrianFilters',\n"
                + "    'hadrianServices',\n"
                + "    'ui.ace'\n"
                + "]);\n"
                + "\n"
                + "let b = 42;";

        String expected = "var hadrianApp = angular.module('HadrianApp', [ 'ngRoute', 'ngAnimate', 'ui.bootstrap', 'hadrianDirectives', 'hadrianControllers', 'hadrianFilters', 'hadrianServices', 'ui.ace']);\n"
                + "let b = 42;";

        String compressed = CachedContent.compressionJS(orig);
        Assert.assertEquals(expected, compressed);
    }

}
