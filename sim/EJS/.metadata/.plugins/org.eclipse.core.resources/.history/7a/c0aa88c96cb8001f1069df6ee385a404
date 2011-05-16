/*
 * Copyright 2007, 2008, 2009 Ange Optimization ApS
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
package dk.ange.octave.io.impl;

import junit.framework.TestCase;
import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.io.OctaveIO;
import dk.ange.octave.type.OctaveFunctionHandle;
import dk.ange.octave.type.OctaveObject;

/**
 * Test read/write of {@link OctaveFunctionHandle}
 */
public class TestIoFunctionHandle extends TestCase {

    /** Test */
    public void testToString() {
        final OctaveObject fh = new OctaveFunctionHandle("@(x) sqrt (1 - x ^ 2)");
        assertEquals("" + //
                "# name: ans\n" + //
                "# type: function handle\n" + //
                "@<anonymous>\n" + //
                "@(x) sqrt (1 - x ^ 2)\n" + //
                "# length: 0\n" + //
                "", OctaveIO.toText(fh));
    }

    /** Test */
    public void testToOctave() {
        final OctaveObject fh = new OctaveFunctionHandle("@(x) sqrt (1 - x ^ 2)");
        assertEquals("" + //
                "# name: fh\n" + //
                "# type: function handle\n" + //
                "@<anonymous>\n" + //
                "@(x) sqrt (1 - x ^ 2)\n" + //
                "# length: 0\n" + //
                "", OctaveIO.toText(fh, "fh"));
    }

    /** Test */
    public void testOctaveConnection() {
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
        if (octave.getVersion().equals("3.0.5")) {
            octave.close();
            return; // Skip test on octave 3.0.5
        }
        final OctaveObject fh1 = new OctaveFunctionHandle("@(x) sqrt (1 - x ^ 2)");
        octave.put("fh", fh1);
        // TODO enable when reading of OctaveFunctionHandle is done
        // final OctaveObject fh2 = octave.get(OctaveString.class, "fh");
        // assertEquals(s1, s2);
        octave.close();
    }

}
