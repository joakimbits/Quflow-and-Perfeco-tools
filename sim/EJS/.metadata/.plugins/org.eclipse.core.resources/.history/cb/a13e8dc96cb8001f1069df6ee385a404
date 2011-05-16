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
package dk.ange.octave.type;

import junit.framework.TestCase;
import dk.ange.octave.exception.OctaveClassCastException;

/**
 * Test OctaveStruct object
 */
public class TestOctaveStruct extends TestCase {

    /**
     * Test that a copy is taken when getting from struct
     */
    public void testOctaveGetCopy() {
        final OctaveStruct struct = new OctaveStruct();
        struct.set("scalar", Octave.scalar(42));
        final OctaveDouble scalar = struct.get(OctaveDouble.class, "scalar");
        scalar.set(10, 1, 1);
        assertEquals(scalar.get(1, 1), 10.0);
        assertEquals(struct.get(OctaveDouble.class, "scalar").get(1, 1), 42.0);
    }

    /**
     * Test that the correct exception is thrown from OctaveStruct.get()
     */
    public void testClassCast() {
        final OctaveStruct struct = new OctaveStruct();
        final OctaveDouble scalar = Octave.scalar(42);
        struct.set("scalar", scalar);
        try {
            final OctaveCell cell = struct.get(OctaveCell.class, "scalar");
            fail(cell.toString());
        } catch (final OctaveClassCastException e) {
            assertEquals(scalar, e.getOctaveObject());
        }
    }

    /**
     * Test that we can get unknown values
     */
    public void testGetUnknown() {
        final OctaveStruct struct = new OctaveStruct();
        assertNull(struct.get("unknown"));
    }

}
