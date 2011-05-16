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
package dk.ange.octave.type;

import junit.framework.TestCase;

/**
 * Test {@link OctaveInt}
 */
public class TestOctaveInt extends TestCase {

    /**
     * Test equals
     */
    public void testEquals() {
        final OctaveInt s1a = intScalar(1);
        final OctaveInt s1b = intScalar(1);
        final OctaveInt s1c = intScalar(0);
        s1c.set(1, 1, 1);

        assertEquals(s1a, s1b);
        assertEquals(s1a, s1c);
        assertEquals(s1b, s1c);
        assertNotSame(s1a, s1b);
        assertNotSame(s1a, s1c);
        assertNotSame(s1b, s1c);

        final OctaveInt s0 = intScalar(0);
        final OctaveInt s2 = intScalar(2);

        assertFalse(s1a.equals(s0));
        assertFalse(s1a.equals(s2));
    }

    private static OctaveInt intScalar(final int i) {
        return new OctaveInt(new int[] { i }, 1, 1);
    }

    /**
     * Simple test of set and get
     */
    public void testGetAndSet() {
        final OctaveInt matrix = new OctaveInt(3, 6, 5, 4);
        assertEquals(0, matrix.get(2, 5, 2, 3));
        matrix.set(42, 2, 5, 2, 3);
        assertEquals(42, matrix.get(2, 5, 2, 3));
    }

}
