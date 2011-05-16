/*
 * Copyright 2008 Ange Optimization ApS
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
/**
 * @author Kim Hansen
 */
package dk.ange.octave.io.impl;

import java.io.BufferedReader;

import dk.ange.octave.exception.OctaveParseException;
import dk.ange.octave.io.OctaveIO;
import dk.ange.octave.io.spi.OctaveDataReader;
import dk.ange.octave.type.OctaveInt;

/**
 * The reader of matrix
 */
public final class Uint8MatrixReader extends OctaveDataReader {

    @Override
    public String octaveType() {
        return "uint8 matrix";
    }

    @Override
    public OctaveInt read(final BufferedReader reader) {
        final String line = OctaveIO.readerReadLine(reader);
        return readVectorizedMatrix(reader, line);
    }

    private OctaveInt readVectorizedMatrix(final BufferedReader reader, final String ndimsLine) {
        String line;
        final String NDIMS = "# ndims: ";
        line = ndimsLine;
        if (!line.startsWith(NDIMS)) {
            throw new OctaveParseException("Expected '" + NDIMS + "', but got '" + line + "'");
        }
        final int ndims = Integer.parseInt(line.substring(NDIMS.length()));
        line = OctaveIO.readerReadLine(reader);
        final String[] split = line.substring(1).split(" ");
        if (split.length != ndims) {
            throw new OctaveParseException("Expected " + ndims + " dimesion, but got " + (split.length)
                    + " (line was '" + line + "')");
        }
        final int[] size = new int[split.length];
        for (int dim = 0; dim < split.length; dim++) {
            size[dim] = Integer.parseInt(split[dim]);
        }
        final int[] data = new int[product(size)];
        for (int idx = 0; idx < data.length; idx++) {
            line = OctaveIO.readerReadLine(reader);
            data[idx] = line.codePointAt(1);
        }
        return new OctaveInt(data, size);
    }

    /**
     * @param ns
     * @return product of rs
     */
    private static int product(final int... ns) {
        int p = 1;
        for (final int n : ns) {
            p *= n;
        }
        return p;
    }

}
