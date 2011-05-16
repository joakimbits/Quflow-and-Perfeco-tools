/*
 * Copyright 2007, 2008, 2009 Ange Optimization ApS
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
package dk.ange.octave.type;

import java.util.Arrays;

/**
 * A Boolean matrix
 */
public class OctaveSparseBoolean implements OctaveObject {

    private final int rows;

    private final int columns;

    private int nnz;

    private final int[] rowIndexes;

    private final int[] columnIndexes;

    private final boolean[] data;

    private OctaveSparseBoolean(final int rows, final int columns, final int nnz, final int[] rowIndexes,
            final int[] columnIndexes, final boolean[] data) {
        this.rows = rows;
        this.columns = columns;
        this.nnz = nnz;
        this.rowIndexes = rowIndexes;
        this.columnIndexes = columnIndexes;
        this.data = data;
    }

    /**
     * @param rows
     * @param columns
     * @param nnz
     */
    public OctaveSparseBoolean(final int rows, final int columns, final int nnz) {
        this(rows, columns, 0, new int[nnz], new int[nnz], new boolean[nnz]);
    }

    @Override
    public OctaveSparseBoolean shallowCopy() {
        return new OctaveSparseBoolean(rows, columns, nnz, rowIndexes, columnIndexes, data);
    }

    /**
     * @param value
     * @param row
     * @param column
     */
    public void set(final boolean value, final int row, final int column) {
        data[nnz] = value;
        rowIndexes[nnz] = row;
        columnIndexes[nnz] = column;
        ++nnz;
    }

    /**
     * @return the rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * @return the columns
     */
    public int getColumns() {
        return columns;
    }

    /**
     * @return the nnz
     */
    public int getNnz() {
        return nnz;
    }

    /**
     * @return the rowIndexes
     */
    public int[] getRowIndexes() {
        return rowIndexes;
    }

    /**
     * @return the columnIndexes
     */
    public int[] getColumnIndexes() {
        return columnIndexes;
    }

    /**
     * @return the data
     */
    public boolean[] getData() {
        return data;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(columnIndexes);
        result = prime * result + columns;
        result = prime * result + Arrays.hashCode(data);
        result = prime * result + nnz;
        result = prime * result + Arrays.hashCode(rowIndexes);
        result = prime * result + rows;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OctaveSparseBoolean other = (OctaveSparseBoolean) obj;
        if (!Arrays.equals(columnIndexes, other.columnIndexes)) {
            return false;
        }
        if (columns != other.columns) {
            return false;
        }
        if (!Arrays.equals(data, other.data)) {
            return false;
        }
        if (nnz != other.nnz) {
            return false;
        }
        if (!Arrays.equals(rowIndexes, other.rowIndexes)) {
            return false;
        }
        if (rows != other.rows) {
            return false;
        }
        return true;
    }

}
