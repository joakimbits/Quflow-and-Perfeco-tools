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
 * http://kenai.com/projects/javaoctave/pages/SimpleExampleOfJavaOctaveUsage
 */
public class SimpleExampleTest extends TestCase {

    /** Test */
    public void test() {
        // Begin web text
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
        final OctaveDouble a = new OctaveDouble(new double[] { 1, 2, 3, 4 }, 2, 2);
        octave.put("a", a);
        final String func = "" //
                + "function res = my_func(a)\n" //
                + " res = 2 * a;\n" //
                + "endfunction\n" //
                + "";
        octave.eval(func);
        octave.eval("b = my_func(a);");
        final OctaveDouble b = octave.get(OctaveDouble.class, "b");
        octave.close();
        // End web text
        assertEquals(8.0, b.get(2, 2));
    }

}
