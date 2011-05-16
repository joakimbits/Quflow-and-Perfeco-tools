package dk.ange.octave.io;

import java.util.Map;

import junit.framework.TestCase;
import dk.ange.octave.exception.OctaveParseException;
import dk.ange.octave.type.Octave;
import dk.ange.octave.type.OctaveDouble;
import dk.ange.octave.type.OctaveObject;

/**
 * Test {@link OctaveIO}
 */
public class TestOctaveIO extends TestCase {

    /**
     * Test that {@link OctaveIO#readWithName(String)} works and throws the expected on too much data.
     */
    public void testReadWithName() {
        // Data
        final String varName = "x";
        final OctaveDouble varValue = Octave.scalar(42);
        final String input = "" + //
                "# name: x\n" + //
                "# type: scalar\n" + //
                "42\n" + //
                "";
        final String extra = "" + //
                "extra 1\n" + //
                "extra 2\n" + //
                "";
        // Test ok
        final Map<String, OctaveObject> map = OctaveIO.readWithName(input);
        assertTrue(map.containsKey(varName));
        assertEquals(varValue, map.get(varName));
        // Test error
        try {
            OctaveIO.readWithName(input + extra);
        } catch (final OctaveParseException e) {
            assertEquals("Too much data in input, first extra line is 'extra 1'", e.getMessage());
        }
    }

}
