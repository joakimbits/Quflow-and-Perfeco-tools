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

import java.io.IOException;
import java.io.Writer;

import dk.ange.octave.io.spi.OctaveDataWriter;
import dk.ange.octave.type.OctaveSparseBoolean;

/**
 * The writer of {@link OctaveSparseBoolean}
 * 
 * <pre>
 * # name: x
 * # type: sparse bool matrix
 * # nnz: 1
 * # rows: 1
 * # columns: 1
 * 1 1 1
 * </pre>
 */
public final class SparseBooleanWriter extends OctaveDataWriter<OctaveSparseBoolean> {

    @Override
    public Class<OctaveSparseBoolean> javaType() {
        return OctaveSparseBoolean.class;
    }

    @Override
    public void write(final Writer writer, final OctaveSparseBoolean octaveSparseBoolean) throws IOException {
        final int nnz = octaveSparseBoolean.getNnz();
        writer.write("# type: sparse bool matrix\n");
        writer.write("# nnz: " + nnz + "\n");
        writer.write("# rows: " + octaveSparseBoolean.getRows() + "\n");
        writer.write("# columns: " + octaveSparseBoolean.getColumns() + "\n");
        final int[] rowIndexes = octaveSparseBoolean.getRowIndexes();
        final int[] columnIndexes = octaveSparseBoolean.getColumnIndexes();
        final boolean[] data = octaveSparseBoolean.getData();
        for (int n = 0; n < nnz; ++n) {
            writer.write(rowIndexes[n] + " " + columnIndexes[n] + " " + (data[n] ? "1" : "0") + "\n");
        }
    }

}
