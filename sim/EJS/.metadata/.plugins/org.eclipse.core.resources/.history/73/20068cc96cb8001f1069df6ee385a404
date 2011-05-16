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

import java.util.Map;

import junit.framework.TestCase;
import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.io.OctaveIO;
import dk.ange.octave.type.OctaveBoolean;
import dk.ange.octave.type.OctaveObject;
import dk.ange.octave.type.OctaveSparseBoolean;

/**
 * Test read/write of {@link OctaveBoolean}
 */
public class TestIoOctaveSparseBoolean extends TestCase {

    private static final String TEXT_TRUE = "" + //
            "# name: x\n" + //
            "# type: sparse bool matrix\n" + //
            "# nnz: 1\n" + //
            "# rows: 1\n" + //
            "# columns: 1\n" + //
            "1 1 1\n" + //
            "";

    private static final String TEXT_EMPTY = "" + //
            "# name: x\n" + //
            "# type: sparse bool matrix\n" + //
            "# nnz: 0\n" + //
            "# rows: 0\n" + //
            "# columns: 0\n" + //
            "";

    /** */
    public void testReadFalse() {
        final Map<String, OctaveObject> read = OctaveIO.readWithName("" + //
                "# name: x\n" + //
                "# type: sparse bool matrix\n" + //
                "# nnz: 0\n" + //
                "# rows: 1\n" + //
                "# columns: 1\n" + //
                "");
        assertTrue(read.toString(), read.containsKey("x"));
    }

    /** */
    public void testReadTrue() {
        final Map<String, OctaveObject> read = OctaveIO.readWithName(TEXT_TRUE);
        assertTrue(read.toString(), read.containsKey("x"));
    }

    /** */
    public void testWriteTrue() {
        final OctaveSparseBoolean o = new OctaveSparseBoolean(1, 1, 1);
        o.set(true, 1, 1);
        assertEquals(TEXT_TRUE, OctaveIO.toText(o, "x"));
    }

    /** */
    public void testReadEmpty() {
        final Map<String, OctaveObject> read = OctaveIO.readWithName(TEXT_EMPTY);
        assertTrue(read.toString(), read.containsKey("x"));
        assertEquals(new OctaveSparseBoolean(0, 0, 0), read.get("x"));
    }

    /** */
    public void testWriteEmpty() {
        final OctaveSparseBoolean o = new OctaveSparseBoolean(0, 0, 0);
        assertEquals(TEXT_EMPTY, OctaveIO.toText(o, "x"));
    }

    /** */
    public void testReadWrite() {
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();

        if (octave.getVersion().equals("3.0.5")) {
            return; // Skip test on octave 3.0.5
        }

        OctaveObject s;

        octave.eval("x = true(0, 0);");
        octave.eval("s = sparse(x);");
        s = octave.get("s");
        octave.put("c", s);
        octave.eval("assert(c, s);");

        octave.eval("x = true;");
        octave.eval("s = sparse(x);");
        s = octave.get("s");
        octave.put("c", s);
        octave.eval("assert(c, s);");

        octave.eval("x(3, 1) = true;");
        octave.eval("s = sparse(x);");
        s = octave.get("s");
        octave.put("c", s);
        octave.eval("assert(c, s);");

        octave.eval("x(2, 2) = true;");
        octave.eval("s = sparse(x);");
        s = octave.get("s");
        octave.put("c", s);
        octave.eval("assert(c, s);");

        octave.close();
    }

}
