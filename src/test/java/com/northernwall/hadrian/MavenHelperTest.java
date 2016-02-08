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
package com.northernwall.hadrian;

import com.northernwall.hadrian.maven.MavenVersionComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author rthursto
 */
public class MavenHelperTest {
    
    public MavenHelperTest() {
    }
    
    @Test
    public void testComparator() {
        MavenVersionComparator comparator = new MavenVersionComparator();
        
        List<String> versions = new ArrayList<>();
        versions.add("1.2.3");
        versions.add("1.1.5");
        versions.add("1.2.4");
        versions.add("1.13.4");
        versions.add("1.1.six");
        versions.add("1.2.4-SNAPSHOT");
        versions.add("1.1");
        versions.add("0.3.4");
        
        Collections.sort(versions, comparator);
        
        Assert.assertEquals("1.13.4", versions.get(0));
        Assert.assertEquals("1.2.4", versions.get(1));
        Assert.assertEquals("1.2.4-SNAPSHOT", versions.get(2));
        Assert.assertEquals("1.2.3", versions.get(3));
        Assert.assertEquals("1.1", versions.get(4));
        Assert.assertEquals("1.1.six", versions.get(5));
        Assert.assertEquals("1.1.5", versions.get(6));
        Assert.assertEquals("0.3.4", versions.get(7));
    }
}
