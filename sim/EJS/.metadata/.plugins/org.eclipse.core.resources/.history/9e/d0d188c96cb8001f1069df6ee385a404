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
import java.util.TreeMap;

import junit.framework.TestCase;
import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.io.OctaveIO;
import dk.ange.octave.type.OctaveBoolean;
import dk.ange.octave.type.OctaveObject;

/**
 * Test read/write of {@link OctaveBoolean}
 */
public class TestIoOctaveBoolean extends TestCase {

    /**
     * Test
     */
    public void testConstructorIntIntInt() {
        final OctaveBoolean matrix = new OctaveBoolean(3, 4, 2);
        assertEquals("" //
                + "# name: matrix3d\n" //
                + "# type: bool matrix\n" //
                + "# ndims: 3\n" //
                + " 3 4 2\n" //
                + " 0\n 0\n 0\n" //
                + " 0\n 0\n 0\n" //
                + " 0\n 0\n 0\n" //
                + " 0\n 0\n 0\n" //
                + " 0\n 0\n 0\n" //
                + " 0\n 0\n 0\n" //
                + " 0\n 0\n 0\n" //
                + " 0\n 0\n 0\n" //
                + "", OctaveIO.toText(matrix, "matrix3d"));
        matrix.set(true, 1, 3, 2);
        assertEquals("" //
                + "# name: matrix3d\n" //
                + "# type: bool matrix\n" //
                + "# ndims: 3\n" //
                + " 3 4 2\n" //
                + " 0\n 0\n 0\n" //
                + " 0\n 0\n 0\n" //
                + " 0\n 0\n 0\n" //
                + " 0\n 0\n 0\n" //
                + " 0\n 0\n 0\n" //
                + " 0\n 0\n 0\n" //
                + " 1\n 0\n 0\n" //
                + " 0\n 0\n 0\n" //
                + "", OctaveIO.toText(matrix, "matrix3d"));
    }

    /**
     * Test
     */
    public void testConstructor1() {
        final OctaveBoolean matrix = new OctaveBoolean(0, 0, 0);
        assertEquals("# name: matrix3d\n# type: bool matrix\n# ndims: 3\n 0 0 0\n", OctaveIO.toText(matrix, "matrix3d"));
    }

    /**
     * @throws Exception
     */
    public void testConstructor2() throws Exception {
        final OctaveBoolean matrix = new OctaveBoolean(0, 0);
        assertEquals(0, matrix.size(1));
        assertEquals(0, matrix.size(2));
        assertEquals("" + //
                "# name: matrix\n" + //
                "# type: bool matrix\n" + //
                "# rows: 0\n" + //
                "# columns: 0\n" + //
                "" //
        , OctaveIO.toText(matrix, "matrix"));
    }

    /**
     * @throws Exception
     */
    public void testConstructorMatrix() throws Exception {
        final boolean[] numbers = { true, true, false, false, true, true };
        final OctaveBoolean matrix = new OctaveBoolean(numbers, 2, 3);
        assertEquals(2, matrix.size(1));
        assertEquals(3, matrix.size(2));
        assertEquals("" + //
                "# name: mymatrix\n" + //
                "# type: bool matrix\n" + //
                "# rows: 2\n" + //
                "# columns: 3\n" + //
                " 1 0 1\n" + //
                " 1 0 1\n" //
        , OctaveIO.toText(matrix, "mymatrix"));
    }

    /**
     * @throws Exception
     */
    public void testConstructorIntInt() throws Exception {
        final OctaveBoolean matrix = new OctaveBoolean(2, 3);
        assertEquals(2, matrix.size(1));
        assertEquals(3, matrix.size(2));
        assertEquals("" + //
                "# name: matrix\n" + //
                "# type: bool matrix\n" + //
                "# rows: 2\n" + //
                "# columns: 3\n" + //
                " 0 0 0\n" + //
                " 0 0 0\n" //
        , OctaveIO.toText(matrix, "matrix"));
        matrix.set(true, 1, 2);
        assertEquals("" + //
                "# name: myother\n" + //
                "# type: bool matrix\n" + //
                "# rows: 2\n" + //
                "# columns: 3\n" + //
                " 0 1 0\n" + //
                " 0 0 0\n" //
        , OctaveIO.toText(matrix, "myother"));
        matrix.set(true, 2, 1);
        assertEquals("" + //
                "# name: myother\n" + //
                "# type: bool matrix\n" + //
                "# rows: 2\n" + //
                "# columns: 3\n" + //
                " 0 1 0\n" + //
                " 1 0 0\n" //
        , OctaveIO.toText(matrix, "myother"));
        matrix.set(true, 2, 2);
        assertEquals("" + //
                "# name: myother\n" + //
                "# type: bool matrix\n" + //
                "# rows: 2\n" + //
                "# columns: 3\n" + //
                " 0 1 0\n" + //
                " 1 1 0\n" //
        , OctaveIO.toText(matrix, "myother"));
    }

    /**
     * @throws Exception
     */
    public void testGrowth() throws Exception {
        final OctaveBoolean matrix = new OctaveBoolean(0, 0);
        assertEquals(0, matrix.size(1));
        assertEquals(0, matrix.size(2));
        assertEquals("" + //
                "# name: matrix\n" + //
                "# type: bool matrix\n" + //
                "# rows: 0\n" + //
                "# columns: 0\n" //
        , OctaveIO.toText(matrix, "matrix"));
        matrix.set(true, 1, 1);
        assertEquals(1, matrix.size(1));
        assertEquals(1, matrix.size(2));
        assertEquals("" + //
                "# name: matrix\n" + //
                "# type: bool matrix\n" + //
                "# rows: 1\n" + //
                "# columns: 1\n" + //
                " 1\n" //
        , OctaveIO.toText(matrix, "matrix"));
        matrix.set(true, 3, 1);
        assertEquals(3, matrix.size(1));
        assertEquals(1, matrix.size(2));
        assertEquals("" + //
                "# name: matrix\n" + //
                "# type: bool matrix\n" + //
                "# rows: 3\n" + //
                "# columns: 1\n" + //
                " 1\n" + //
                " 0\n" + //
                " 1\n" //
        , OctaveIO.toText(matrix, "matrix"));

        final OctaveBoolean matrix2 = new OctaveBoolean(0, 0);
        matrix2.set(true, 1, 3);
        assertEquals("" + //
                "# name: matrix\n" + //
                "# type: bool matrix\n" + //
                "# rows: 1\n" + //
                "# columns: 3\n" + //
                " 0 0 1\n" + //
                "" //
        , OctaveIO.toText(matrix2, "matrix"));
    }

    /**
     * @throws Exception
     */
    public void testOctaveGet() throws Exception {
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
        octave.eval("m=[true false;false true];");
        final OctaveBoolean m = octave.get(OctaveBoolean.class, "m");
        assertEquals(2, m.size(1));
        assertEquals(2, m.size(2));
        assertEquals(true, m.get(1, 1));
        assertEquals(false, m.get(1, 2));
        assertEquals(false, m.get(2, 1));
        assertEquals(true, m.get(2, 2));
        octave.close();
    }

    /**
     * @throws Exception
     */
    public void testOctaveSetExecGet() throws Exception {
        final boolean[] numbers = { true, false, true, false, true, false };
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
        final OctaveBoolean in = new OctaveBoolean(numbers, 2, 3);
        octave.put("in", in);
        octave.eval("out=in;");
        final OctaveBoolean out = octave.get(OctaveBoolean.class, "out");
        assertEquals(OctaveIO.toText(in), OctaveIO.toText(out));
        octave.eval("slicerow=in(2,:); slicecol=in(:,2);");
        final OctaveBoolean slicerow = octave.get(OctaveBoolean.class, "slicerow");
        final OctaveBoolean slicecol = octave.get(OctaveBoolean.class, "slicecol");
        assertEquals(false, slicerow.get(1, 1));
        assertEquals(false, slicerow.get(1, 2));
        assertEquals(false, slicerow.get(1, 3));
        assertEquals(true, slicecol.get(1, 1));
        assertEquals(false, slicecol.get(2, 1));
        octave.close();
    }

    /**
     * Test
     */
    public void test3dMatrix() {
        final OctaveBoolean matrix3d = new OctaveBoolean(3, 4, 2);
        matrix3d.set(true, 1, 3, 2);
        matrix3d.set(true, 3, 1, 1);
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
        octave.put("matrix3d", matrix3d);
        octave.eval("x1 = matrix3d(:,:,1);");
        octave.eval("x2 = matrix3d(:,:,2);");
        octave.eval("x3 = matrix3d(:,3,:);");
        octave.eval("x4 = matrix3d(3,:,:);");
        final OctaveBoolean x1 = octave.get(OctaveBoolean.class, "x1");
        final OctaveBoolean x2 = octave.get(OctaveBoolean.class, "x2");
        final OctaveBoolean x3 = octave.get(OctaveBoolean.class, "x3");
        final OctaveBoolean x4 = octave.get(OctaveBoolean.class, "x4");
        octave.close();
        assertEquals(false, x1.get(1, 3));
        assertEquals(true, x1.get(3, 1));
        assertEquals(true, x2.get(1, 3));
        assertEquals(false, x2.get(3, 1));
        assertEquals(true, x3.get(1, 1, 2));
        assertEquals(true, x4.get(1, 1, 1));
    }

    /** Test */
    public void testNdMatrix() {
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
        final TreeMap<String, OctaveObject> vars = new TreeMap<String, OctaveObject>();
        final boolean[] bigdata = new boolean[2 * 3 * 4];
        for (int idx = 0; idx < bigdata.length; idx++) {
            bigdata[idx] = idx % 2 == 0;
        }
        final boolean[] data2d = { true, false, true, false, true, false };
        final boolean[] datascalar = { true };
        vars.put("bigmatrix", new OctaveBoolean(bigdata, 1, 2, 3, 4));
        vars.put("matrix2d", new OctaveBoolean(data2d, 2, 3));
        vars.put("matrixscalar", new OctaveBoolean(datascalar, 1, 1));
        vars.put("matrixzero", new OctaveBoolean(0, 0, 0, 0));
        vars.put("matrixzero2d", new OctaveBoolean(0, 0));
        octave.putAll(vars);
        final OctaveBoolean matrixzero = octave.get(OctaveBoolean.class, "matrixzero");
        final OctaveBoolean matrix2d = octave.get(OctaveBoolean.class, "matrix2d");
        final OctaveBoolean bigmatrix = octave.get(OctaveBoolean.class, "bigmatrix");
        final OctaveBoolean matrixzero2d = octave.get(OctaveBoolean.class, "matrixzero2d");
        final OctaveBoolean matrixscalar = octave.get(OctaveBoolean.class, "matrixscalar");
        assertEquals(matrixzero, vars.get("matrixzero"));
        assertEquals(matrixzero2d, vars.get("matrixzero2d"));
        assertEquals(matrixscalar, vars.get("matrixscalar"));
        assertEquals(matrix2d, vars.get("matrix2d"));
        assertEquals(bigmatrix, vars.get("bigmatrix"));
        octave.close();

        assertEquals("" + //
                "# name: matrixzero2d\n" + //
                "# type: bool matrix\n" + //
                "# rows: 0\n" + //
                "# columns: 0\n" //
        , OctaveIO.toText(matrixzero2d, "matrixzero2d"));

        assertEquals("" + //
                "# name: matrixzero\n" + //
                "# type: bool matrix\n" + //
                "# ndims: 4\n" + //
                " 0 0 0 0\n" // 
        , OctaveIO.toText(matrixzero, "matrixzero"));

        assertEquals("" + //
                "# name: matrixscalar\n" + //
                "# type: bool matrix\n" + //
                "# rows: 1\n" + //
                "# columns: 1\n" + //
                " 1\n" //
        , OctaveIO.toText(matrixscalar, "matrixscalar"));

        assertEquals("" + //
                "# name: matrix2d\n" + //
                "# type: bool matrix\n" + //
                "# rows: 2\n" + //
                "# columns: 3\n" + //
                " 1 1 1\n" + //
                " 0 0 0\n" //
        , OctaveIO.toText(matrix2d, "matrix2d"));

        assertEquals("" + //
                "# name: bigmatrix\n" + //
                "# type: bool matrix\n" + //
                "# ndims: 4\n" + //
                " 1 2 3 4\n" + //
                " 1\n" + //
                " 0\n" + //
                " 1\n" + //
                " 0\n" + //
                " 1\n" + //
                " 0\n" + //
                " 1\n" + //
                " 0\n" + //
                " 1\n" + //
                " 0\n" + //
                " 1\n" + //
                " 0\n" + //
                " 1\n" + //
                " 0\n" + //
                " 1\n" + //
                " 0\n" + //
                " 1\n" + //
                " 0\n" + //
                " 1\n" + //
                " 0\n" + //
                " 1\n" + //
                " 0\n" + //
                " 1\n" + //
                " 0\n" //
        , OctaveIO.toText(bigmatrix, "bigmatrix"));
    }

    /** */
    public void testWriteRead() {
        final OctaveBoolean boolean1 = new OctaveBoolean(0, 0);
        boolean1.set(true, 2, 2);

        final String text = OctaveIO.toText(boolean1);

        final Map<String, OctaveObject> read = OctaveIO.readWithName(text);
        assertTrue(read.containsKey("ans"));
        assertEquals(boolean1, read.get("ans"));
    }

    /** */
    public void testRead() {
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();

        final OctaveBoolean bool = new OctaveBoolean(0, 0);
        octave.eval("x = true(0, 0);");
        assertEquals(bool, octave.get("x"));

        bool.set(true, 1, 1);
        octave.eval("x = true;");
        assertEquals(bool, octave.get("x"));

        bool.set(true, 3, 1);
        octave.eval("x(3, 1) = true;");
        assertEquals(bool, octave.get("x"));

        bool.set(true, 2, 2);
        octave.eval("x(2, 2) = true;");
        assertEquals(bool, octave.get("x"));

        final OctaveBoolean bool3 = new OctaveBoolean(0, 0, 0);
        bool3.set(true, 2, 2, 2);
        octave.eval("x3(2, 2, 2) = true;");
        assertEquals(bool3, octave.get("x3"));

        octave.close();
    }

    /**
     * Test that we handle the special single boolean case. Type will be "bool", not "bool matrix".
     */
    public void testReadSingle() {
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();

        final OctaveBoolean bool = new OctaveBoolean(0, 0);
        bool.set(true, 1, 1);
        octave.eval("x = true;");
        assertEquals(bool, octave.get("x"));

        octave.close();
    }

}
