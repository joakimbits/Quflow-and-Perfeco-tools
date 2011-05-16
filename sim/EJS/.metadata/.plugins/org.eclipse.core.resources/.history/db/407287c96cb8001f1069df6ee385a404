/*
 * Copyright 2009 Ange Optimization ApS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.ange.octave;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Test getVersion
 */
public class TestVersion extends TestCase {

    /**
     * Test getVersion
     */
    public void testVersion() {
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
        octave.getVersion();
    }

    /**
     * Test that the version is a version we know. If this test fails the fix will usually be to add the new version to
     * the Set of known versions.
     */
    public void testKnownVersion() {
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
        final String version = octave.getVersion();
        final Set<String> knownVersions = new HashSet<String>(Arrays.asList("3.0.5", "3.2.3", "3.2.4"));
        assertTrue("Version '" + version + "' is not known", knownVersions.contains(version));
    }

}
