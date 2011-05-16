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

/**
 * Test OctaveCell
 */
public class TestOctaveCell extends TestCase {

    /**
     * Tests that the get methods returns a copy
     */
    public void testReturnCopy() {
        final OctaveCell cell = new OctaveCell(0, 0);
        cell.set(Octave.scalar(2), 1, 1);
        final OctaveDouble scalar = cell.get(OctaveDouble.class, 1, 1);
        scalar.set(10.0, 1, 1);
        assertEquals(scalar.get(1, 1), 10.0);
        assertEquals(cell.get(OctaveDouble.class, 1, 1).get(1, 1), 2.0);
    }

    /**
     * Test that equality of the 1x1 empty cell works
     */
    public void testEquality() {
        final OctaveCell cell1 = new OctaveCell(1, 1);
        final OctaveCell cell2 = new OctaveCell(1, 1);
        cell2.set(cell1.get(1, 1), 1, 1);
        assertEquals(cell1, cell2);
    }

}
