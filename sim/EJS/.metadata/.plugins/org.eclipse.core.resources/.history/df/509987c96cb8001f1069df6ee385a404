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
package dk.ange.octave.examples;

import junit.framework.TestCase;
import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.type.OctaveDouble;

/**
 * http://kenai.com/projects/javaoctave/pages/SimpleExampleOfSolvingAnODE
 */
public class OdeExampleTest extends TestCase {

    /** Test */
    public void test() {
        // Begin web text
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
        octave.eval("x = 0:0.01:1;");
        octave.eval("t = lsode(@(x,t) x**2+t**2, 0, x);");
        final OctaveDouble t = octave.get(OctaveDouble.class, "t");
        octave.close();
        final double[] result = t.getData();
        // End web text
        assertEquals(101, result.length);
    }

}
