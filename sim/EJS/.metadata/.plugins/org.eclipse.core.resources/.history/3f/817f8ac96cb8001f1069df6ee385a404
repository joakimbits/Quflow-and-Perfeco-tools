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
package dk.ange.octave.io.impl;

import junit.framework.TestCase;
import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.type.OctaveComplex;

/**
 * Test read/write of {@link OctaveComplex}
 */
public class TestIoOctaveComplex extends TestCase {

    /** Test */
    public void testGetScalar() {
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
        octave.eval("y = 1.2 + 3.4i;");
        final OctaveComplex c = octave.get(OctaveComplex.class, "y");
        assertEquals(1.2, c.getReal(1, 1), 1e-10);
        assertEquals(3.4, c.getImag(1, 1), 1e-10);
        octave.close();
    }

    /** Test */
    public void testGet2dMatrix() {
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
        octave.eval("y = [ 1.1 1.1i ; 2.2 2.2i ];");
        final OctaveComplex c = octave.get(OctaveComplex.class, "y");
        assertEquals(1.1, c.getReal(1, 1), 1e-10);
        assertEquals(0.0, c.getImag(1, 1), 1e-10);
        assertEquals(0.0, c.getReal(1, 2), 1e-10);
        assertEquals(1.1, c.getImag(1, 2), 1e-10);
        assertEquals(2.2, c.getReal(2, 1), 1e-10);
        assertEquals(0.0, c.getImag(2, 1), 1e-10);
        assertEquals(0.0, c.getReal(2, 2), 1e-10);
        assertEquals(2.2, c.getImag(2, 2), 1e-10);
        octave.close();
    }

    /** Test that a real number also works in the complex code */
    public void testGetReal() {
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
        octave.eval("y = 1.2;");
        final OctaveComplex c = octave.get(OctaveComplex.class, "y");
        assertEquals(1.2, c.getReal(1, 1), 1e-10);
        assertEquals(0, c.getImag(1, 1), 1e-10);
        octave.close();
    }

}
