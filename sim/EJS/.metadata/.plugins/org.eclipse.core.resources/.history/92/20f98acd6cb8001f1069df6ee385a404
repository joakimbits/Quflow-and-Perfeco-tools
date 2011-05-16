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

import dk.ange.octave.io.OctaveIO;
import dk.ange.octave.io.spi.OctaveDataReader;
import dk.ange.octave.type.OctaveComplex;

/**
 * Reader of "complex scalar"
 * 
 * Format is:
 * 
 * <pre>
 * # type: complex scalar
 * (1.2,3.4)
 * </pre>
 */
public final class ComplexScalarReader extends OctaveDataReader {

    @Override
    public String octaveType() {
        return "complex scalar";
    }

    @Override
    public OctaveComplex read(final BufferedReader reader) {
        final String line = OctaveIO.readerReadLine(reader);
        final int commaIndex = line.indexOf(',');
        final double real = ScalarReader.parseDouble(line.substring(1, commaIndex));
        final double imag = ScalarReader.parseDouble(line.substring(commaIndex + 1, line.length() - 1));
        final OctaveComplex complex = new OctaveComplex(1, 1);
        complex.setReal(real, 1, 1);
        complex.setImag(imag, 1, 1);
        return complex;
    }

}
