/*
 * Copyright 2008, 2009 Ange Optimization ApS
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

import java.io.IOException;
import java.io.Writer;

import dk.ange.octave.io.OctaveIO;
import dk.ange.octave.io.spi.OctaveDataWriter;
import dk.ange.octave.type.OctaveCell;
import dk.ange.octave.type.OctaveObject;

/**
 * The writer of OctaveCell
 */
public final class CellWriter extends OctaveDataWriter<OctaveCell> {

    @Override
    public Class<OctaveCell> javaType() {
        return OctaveCell.class;
    }

    @Override
    public void write(final Writer writer, final OctaveCell octaveCell) throws IOException {
        final int rows = octaveCell.size(1);
        final int columns = octaveCell.size(2);
        writer.write("# type: cell\n");
        writer.write("# rows: " + rows + "\n");
        writer.write("# columns: " + columns + "\n");
        for (int c = 1; c <= columns; ++c) {
            for (int r = 1; r <= rows; ++r) {
                final OctaveObject value = octaveCell.get(r, c);
                OctaveIO.write(writer, "<cell-element>", value);
            }
            writer.write("\n");
        }
    }

}
