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

import java.io.BufferedReader;
import java.io.StringReader;

import junit.framework.Assert;
import junit.framework.TestCase;
import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.exception.OctaveParseException;
import dk.ange.octave.io.OctaveIO;
import dk.ange.octave.type.Octave;
import dk.ange.octave.type.OctaveCell;
import dk.ange.octave.type.OctaveDouble;
import dk.ange.octave.type.OctaveObject;
import dk.ange.octave.type.OctaveString;
import dk.ange.octave.type.OctaveStruct;

/**
 * Test I/O on OctaveStruct
 */
public class TestIoOctaveStruct extends TestCase {

    /**
     */
    public void testConstructor() {
        final OctaveObject struct = new OctaveStruct();
        Assert.assertEquals("# name: mystruct\n# type: struct\n# length: 0\n", OctaveIO.toText(struct, "mystruct"));
    }

    /**
     */
    public void testSet() {
        final OctaveStruct struct1 = new OctaveStruct();
        struct1.set("a", Octave.scalar(42));
        Assert.assertEquals("" + //
                "# name: mystruct\n" + //
                "# type: struct\n" + //
                "# length: 1\n" + //
                "# name: a\n" + //
                "# type: cell\n" + //
                "# rows: 1\n" + //
                "# columns: 1\n" + //
                "# name: <cell-element>\n" + //
                "# type: scalar\n" + //
                "42.0\n" + //
                "\n" + //
                "", OctaveIO.toText(struct1, "mystruct"));
        final OctaveStruct struct2 = new OctaveStruct();
        final OctaveCell octaveCell = new OctaveCell(0, 0);
        octaveCell.set(Octave.scalar(42), 1, 1);
        struct2.set("mycell", octaveCell);
        Assert.assertEquals("" + //
                "# name: mystruct\n" + //
                "# type: struct\n" + //
                "# length: 1\n" + //
                "# name: mycell\n" + //
                "# type: cell\n" + //
                "# rows: 1\n" + //
                "# columns: 1\n" + //
                "# name: <cell-element>\n" + //
                "# type: cell\n" + //
                "# rows: 1\n" + //
                "# columns: 1\n" + //
                "# name: <cell-element>\n" + //
                "# type: scalar\n" + //
                "42.0\n" + //
                "\n" + //
                "\n" + //
                "", OctaveIO.toText(struct2, "mystruct"));
    }

    /**
     */
    public void testOctaveConnection() {
        final OctaveStruct struct = new OctaveStruct();
        struct.set("scalar", Octave.scalar(42));
        final OctaveStruct nested_struct = new OctaveStruct();
        nested_struct.set("string", new OctaveString("a cheese called Horace"));
        struct.set("mynestedstruct", nested_struct);

        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
        octave.put("mystruct", struct);
        final OctaveStruct mystruct_copy = octave.get(OctaveStruct.class, "mystruct");
        Assert.assertEquals(struct, mystruct_copy);
    }

    /**
     * Test
     */
    public void testMatices() {
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
        octave.eval("s = struct();");
        final int[] i123 = { 1, 2, 3 };
        for (final int i : i123) {
            octave.eval(setMatrix(i));
            for (final int j : i123) {
                octave.eval(setMatrix(i, j));
                for (final int k : i123) {
                    octave.eval(setMatrix(i, j, k));
                    for (final int l : i123) {
                        octave.eval(setMatrix(i, j, k, l));
                    }
                }
            }
        }
        final OctaveStruct s1 = octave.get(OctaveStruct.class, "s");
        octave.put("s1", s1);
        octave.eval("t = 1.0*isequal(s, s1);"); // "1.0*" is a typecast from bool to scalar
        final OctaveDouble t = octave.get(OctaveDouble.class, "t");
        assertEquals(1.0, t.get(1, 1));
        final OctaveStruct s2 = octave.get(OctaveStruct.class, "s1");
        assertEquals(s1, s2);
        octave.close();
    }

    private String setMatrix(final int... sizes) {
        final StringBuilder b = new StringBuilder();
        b.append("s.x");
        for (final int s : sizes) {
            b.append(Integer.toString(s));
        }
        b.append(" = round(1000*rand(");
        boolean first = true;
        for (final int s : sizes) {
            if (first) {
                first = false;
            } else {
                b.append(", ");
            }
            b.append(Integer.toString(s));
        }
        b.append("))/1000;");
        return b.toString();
    }

    /**
     * Test that the reader does not understand 1x2 cells
     */
    public void testMatrixStruct() {
        final String input = "" //
                + "# type: struct\n" //
                + "# length: 1\n" //
                + "# name: y\n" //
                + "# type: cell\n" //
                + "# rows: 1\n" //
                + "# columns: 2\n" //
                + "# name: <cell-element>\n" //
                + "# type: scalar\n" //
                + "1\n" //
                + "\n" //
                + "# name: <cell-element>\n" //
                + "# type: scalar\n" //
                + "2\n" //
                + "\n" //
                + "\n" //
                + "";
        try {
            OctaveIO.read(new BufferedReader(new StringReader(input)));
            fail();
        } catch (final RuntimeException e) {
            assertEquals(OctaveParseException.class, e.getClass());
        }
    }

    /**
     * @throws Exception
     */
    public void testWrite() throws Exception {
        final OctaveStruct struct = new OctaveStruct();
        struct.set("x", Octave.scalar(42));

        assertEquals("" + //
                "# name: ans\n" + //
                "# type: struct\n" + //
                "# length: 1\n" + //

                "# name: x\n" + //
                "# type: cell\n" + //
                "# rows: 1\n" + //
                "# columns: 1\n" + //
                "# name: <cell-element>\n" + //
                "# type: scalar\n" + //
                "42.0\n" + //
                "\n" + //

                "", OctaveIO.toText(struct));

        struct.set("y", Octave.scalar(43));

        assertEquals("" + //
                "# name: ans\n" + //
                "# type: struct\n" + //
                "# length: 2\n" + //

                "# name: y\n" + //
                "# type: cell\n" + //
                "# rows: 1\n" + //
                "# columns: 1\n" + //
                "# name: <cell-element>\n" + //
                "# type: scalar\n" + //
                "43.0\n" + //
                "\n" + //

                "# name: x\n" + //
                "# type: cell\n" + //
                "# rows: 1\n" + //
                "# columns: 1\n" + //
                "# name: <cell-element>\n" + //
                "# type: scalar\n" + //
                "42.0\n" + //
                "\n" + //

                "", OctaveIO.toText(struct));
    }

    /**
     * @throws Exception
     */
    public void testWriteRead() throws Exception {
        final OctaveStruct struct = new OctaveStruct();
        struct.set("x", Octave.scalar(42));
        struct.set("y", new OctaveString("y"));
        struct.set("z", new OctaveString("z"));

        final String text = OctaveIO.toText(struct);
        final BufferedReader bufferedReader = new BufferedReader(new StringReader(text));

        assertEquals("# name: ans", bufferedReader.readLine());
        assertEquals(struct, OctaveIO.read(bufferedReader));
        assertEquals(-1, bufferedReader.read()); // Check end of file
    }

}
