/*
 * Copyright 2007, 2008 Ange Optimization ApS
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
/**
 * @author Kim Hansen
 */
package dk.ange.octave.io;

import junit.framework.TestCase;
import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.type.Octave;
import dk.ange.octave.type.OctaveDouble;

/** Tests */
public class TestUnknownVar extends TestCase {

    /**
     * Test Octave.get() on unknown var
     */
    public void testGetUnknownVar() {
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
        final OctaveDouble x1 = octave.get(OctaveDouble.class, "x");
        assertNull(x1);
        final OctaveDouble x = Octave.scalar(42);
        octave.put("x", x);
        final OctaveDouble x2 = octave.get(OctaveDouble.class, "x");
        assertEquals(x, x2);
        octave.close();
    }

}
