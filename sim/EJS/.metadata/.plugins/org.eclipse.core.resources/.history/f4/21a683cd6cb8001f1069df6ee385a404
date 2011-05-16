package dk.ange.octave;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Pattern;

import dk.ange.octave.type.OctaveCell;
import dk.ange.octave.type.OctaveString;

/**
 * Small utility functions that can be used with JavaOctave.
 * 
 * Holder class for static functions.
 */
public final class OctaveUtils {

    private static final Random random = new Random();

    private OctaveUtils() {
        throw new UnsupportedOperationException("Do not instantiate");
    }

    /**
     * @param octave
     * @return list of variables
     */
    public static Collection<String> listVars(final OctaveEngine octave) {
        final String varName = randomVarName();
        octave.eval(String.format("" + //
                "%1$s = cell(0, 1);\n" + //
                "if strncmp(OCTAVE_VERSION(), \"3.0.\", 4)\n" + //
                "  %1$s_1 = whos -v;\n" + // version 3.0
                "else\n" + //
                "  %1$s_1 = whos;\n" + // version 3.2 (and other)
                "endif\n" + //
                "for %1$s_2 = 1 : numel(%1$s_1)\n" + //
                "  %1$s{%1$s_2} = %1$s_1(%1$s_2).name;\n" + //
                "endfor\n" + //
                "clear %1$s_1 %1$s_2\n" + //
                "", varName));
        final OctaveCell data = octave.get(OctaveCell.class, varName);
        octave.eval("clear " + varName);
        final Collection<String> collection = new HashSet<String>();
        final Pattern pattern = Pattern.compile("javaoctave_[0-9a-f]{12}_eval");
        for (int i = 1; i <= data.size(2); ++i) {
            final String name = data.get(OctaveString.class, 1, i).getString();
            if (varName.equals(name) || "__nargin__".equals(name) || "ans".equals(name)
                    || pattern.matcher(name).matches()) {
                continue;
            }
            collection.add(name);
        }
        return collection;
    }

    private static synchronized int nextInt() {
        return random.nextInt(1 << 30);
    }

    private static String randomVarName() {
        return String.format("_OctaveUtils_%d", nextInt());
    }

}
