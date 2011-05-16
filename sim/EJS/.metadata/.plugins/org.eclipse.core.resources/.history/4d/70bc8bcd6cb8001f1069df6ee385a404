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
package dk.ange.octave.io.impl;

import java.io.BufferedReader;

import dk.ange.octave.exception.OctaveParseException;
import dk.ange.octave.io.OctaveIO;
import dk.ange.octave.io.spi.OctaveDataReader;
import dk.ange.octave.type.OctaveString;

/**
 * The reader of string
 */
public class OctaveStringReader extends OctaveDataReader {

    @Override
    public String octaveType() {
        return "string";
    }

    @Override
    public OctaveString read(final BufferedReader reader) {
        final String elements = OctaveIO.readerReadLine(reader);
        final StringBuilder builder = new StringBuilder();
        if (elements.equals("# elements: 0")) {
            // Do nothing, this is the empty string
        } else if (elements.equals("# elements: 1")) {
            final String lengthString = OctaveIO.readerReadLine(reader);
            if (!lengthString.startsWith("# length: ")) {
                throw new OctaveParseException("Parse error in String, line='" + lengthString + "'");
            }
            final int length = Integer.parseInt(lengthString.substring(10));
            boolean first = true;
            while (builder.length() < length) {
                if (!first) {
                    builder.append('\n');
                }
                builder.append(OctaveIO.readerReadLine(reader));
                first = false;
            }
            if (builder.length() != length) {
                throw new OctaveParseException("Unexpected length of string read. expected=" + length + ", actual="
                        + builder.length());
            }
        } else {
            throw new OctaveParseException("Expected elements to be 0 or 1, '" + elements + "'");
        }
        return new OctaveString(builder.toString());
    }
}
