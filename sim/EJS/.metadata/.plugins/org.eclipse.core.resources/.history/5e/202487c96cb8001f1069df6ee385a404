package dk.ange.octave;

import java.util.Collection;

import junit.framework.TestCase;

/**
 * Test {@link OctaveUtils}
 */
public class TestOctaveUtils extends TestCase {

    /**
     * @throws Exception
     */
    public void testListVarsEmpty() throws Exception {
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
        final Collection<String> collection = OctaveUtils.listVars(octave);
        assertEquals(collection.toString(), 0, collection.size());
        octave.close();
    }

    /**
     * @throws Exception
     */
    public void testListVarsOneTwo() throws Exception {
        final OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();

        octave.eval("my_var = 42;");
        final Collection<String> collection1 = OctaveUtils.listVars(octave);
        assertEquals(collection1.toString(), 1, collection1.size());

        octave.eval("1 + 2;");
        octave.eval("my_other_var = 42;");
        final Collection<String> collection2 = OctaveUtils.listVars(octave);
        assertEquals(collection2.toString(), 2, collection2.size());

        octave.close();
    }

}
