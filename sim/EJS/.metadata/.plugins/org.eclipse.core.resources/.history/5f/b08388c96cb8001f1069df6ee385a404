/*
 * Copyright 2010 Ange Optimization ApS
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
import dk.ange.octave.type.OctaveFake;
import dk.ange.octave.type.OctaveObject;

/**
 * Test read/write of {@link OctaveFake}
 */
public class TestIoFakeRange extends TestCase {

    /** */
    public void testReadWrite() {
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();

        OctaveObject x;

        octave.eval("x = 1:10;");
        x = octave.get("x");
        octave.put("c", x);
        octave.eval("assert(c, x);");

        octave.close();
    }

}
