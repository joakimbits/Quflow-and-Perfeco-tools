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
package dk.ange.octave.io.impl;

import java.io.BufferedReader;

import dk.ange.octave.exception.OctaveParseException;
import dk.ange.octave.io.OctaveIO;
import dk.ange.octave.io.spi.OctaveDataReader;
import dk.ange.octave.type.OctaveComplex;

/**
 * Reader of "complex matrix"
 * 
 * Format is:
 * 
 * <pre>
 * # type: complex matrix
 * # rows: 2
 * # columns: 2
 *  (1.1,0) (0,1.1)
 *  (2.2,0) (0,2.2)
 * </pre>
 */
public final class ComplexMatrixReader extends OctaveDataReader {

    @Override
    public String octaveType() {
        return "complex matrix";
    }

    @Override
    public OctaveComplex read(final BufferedReader reader) {
        final int rows = parseRows(reader);
        final int columns = parseColumns(reader);
        final OctaveComplex complex = new OctaveComplex(rows, columns);
        for (int r = 1; r <= rows; ++r) {
            final String line = OctaveIO.readerReadLine(reader);
            final String[] split = line.split(" ");
            if (split.length != columns + 1) {
                throw new OctaveParseException("Error in complex matrix-format: '" + line + "'");
            }
            for (int c = 1; c < split.length; c++) {
                final int commaIndex = split[c].indexOf(',');
                final double real = ScalarReader.parseDouble(split[c].substring(1, commaIndex));
                final double imag = ScalarReader.parseDouble(split[c].substring(commaIndex + 1, split[c].length() - 1));
                complex.setReal(real, r, c);
                complex.setImag(imag, r, c);
            }
        }
        return complex;
    }

    /**
     * @param reader
     * @return the number of rows
     */
    public int parseRows(final BufferedReader reader) {
        final String line = OctaveIO.readerReadLine(reader);
        if (!line.startsWith("# rows: ")) {
            throw new OctaveParseException("Expected <# rows: > got <" + line + ">");
        }
        return Integer.valueOf(line.substring(8));
    }

    /**
     * @param reader
     * @return the number of columns
     */
    public int parseColumns(final BufferedReader reader) {
        final String line = OctaveIO.readerReadLine(reader);
        if (!line.startsWith("# columns: ")) {
            throw new OctaveParseException("Expected <# columns: > got <" + line + ">");
        }
        return Integer.valueOf(line.substring(11));
    }

}
