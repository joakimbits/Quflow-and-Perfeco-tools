/*
 * Copyright 2010 Ange Optimization ApS
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
import dk.ange.octave.type.OctaveFake;

/**
 * The reader of a range.
 * 
 * <pre>
 * # type: range
 * # base, limit, increment
 * 1 10 1
 */
public final class FakeRangeReader extends OctaveDataReader {

    @Override
    public String octaveType() {
        return "range";
    }

    @Override
    public OctaveFake read(final BufferedReader reader) {
        final String line1 = OctaveIO.readerReadLine(reader);
        final String line2 = OctaveIO.readerReadLine(reader);
        return new OctaveFake("# type: range\n" + line1 + "\n" + line2 + "\n");
    }

}
