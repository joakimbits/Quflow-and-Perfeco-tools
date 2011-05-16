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
package dk.ange.octave.type.matrix;

import java.util.Arrays;

/**
 * A general matrix that does not even know that it is an array it stores its data in.
 * 
 * @param <D>
 *            an array
 */
abstract public class AbstractGenericMatrix<D> {

    /**
     * The dimensions, rows x columns x depth x ....
     */
    protected final int[] size;

    /**
     * The data, vectorized.
     */
    protected D data;

    /**
     * Constructor that creates new blank matrix
     * 
     * @param size
     */
    protected AbstractGenericMatrix(final int... size) {
        this.size = size.clone();
        checkSize();
        this.data = newD(product(size));
    }

    /**
     * Constructor that reuses data in the new object
     * 
     * @param data
     * @param size
     */
    protected AbstractGenericMatrix(final D data, final int... size) {
        this.size = size;
        checkSize();
        this.data = data;
        checkDataSize();
    }

    /**
     * Copy constructor
     * 
     * @param o
     */
    protected AbstractGenericMatrix(final AbstractGenericMatrix<D> o) {
        this.size = o.size.clone();
        this.data = newD(product(size));
        System.arraycopy(o.data, 0, data, 0, product(size));
    }

    private void checkSize() throws IllegalArgumentException {
        if (size.length == 0) {
            throw new IllegalArgumentException("no size");
        }
        if (size.length < 2) {
            throw new IllegalArgumentException("size must have a least 2 dimenstions");
        }
        for (final int s : size) {
            if (s < 0) {
                throw new IllegalArgumentException("element in size less than zero. =" + s);
            }
        }
    }

    private void checkDataSize() {
        if (product(size) > dataLength()) {
            final StringBuilder text = new StringBuilder();
            text.append("length of data(");
            text.append(dataLength());
            text.append(") is smaller than size(");
            text.append("[");
            boolean first = true;
            for (final int i : size) {
                if (first) {
                    first = false;
                } else {
                    text.append(", ");
                }
                text.append(i);
            }
            text.append("]");
            text.append(")");
            throw new IllegalArgumentException(text.toString());
        }
    }

    /**
     * @param size_
     * @return new D[size_]
     */
    abstract protected D newD(int size_);

    /**
     * @return data_.length
     */
    abstract protected int dataLength();

    /**
     * Fill data with the default value between fromIndex and toIndex.
     * 
     * Arrays.fill(data, fromIndex, toIndex, default);
     * 
     * @param fromIndex
     * @param toIndex
     */
    abstract protected void dataFillInit(int fromIndex, int toIndex);

    /**
     * @param usedLength
     * @param otherData
     * @return true if the used part of data is equal to otherData
     */
    abstract protected boolean dataEquals(int usedLength, D otherData);

    /**
     * @param ns
     * @return product of ns
     */
    private static int product(final int... ns) {
        int p = 1;
        for (final int n : ns) {
            p *= n;
        }
        return p;
    }

    /**
     * Resize matrix up to include pos
     * 
     * @param pos
     */
    public void resizeUp(final int... pos) {
        if (size.length != pos.length) {
            throw new UnsupportedOperationException("Change in number of dimensions not supported (" + size.length
                    + "!=" + pos.length + ")");
        }
        // Resize by each dimension. This is not the optimal way to do it, but it works.
        for (int dim = 0; dim < size.length; ++dim) {
            if (pos[dim] > size[dim]) {
                resizeAlongOneDimension(dim, pos[dim]);
            }
        }
    }

    /**
     * Do the resizing along a single dimension
     * 
     * @param dim
     * @param newSizeDim
     */
    private void resizeAlongOneDimension(final int dim, final int newSizeDim) {
        // Calculate block size and used length of old and new array
        int oldBlockSize = size[dim];
        int newBlockSize = newSizeDim;
        for (int d = 0; d < dim; ++d) {
            oldBlockSize *= size[d];
            newBlockSize *= size[d];
        }
        int oldLength = oldBlockSize;
        int newLength = newBlockSize;
        for (int d = dim + 1; d < size.length; ++d) {
            oldLength *= size[d];
            newLength *= size[d];
        }

        // Set size
        size[dim] = newSizeDim;

        // Do nothing when resizing to yet another zero element matrix
        if (newLength == 0) {
            return;
        }

        // Move data around
        if (dataLength() < newLength) {
            // Create new array and copy data into it
            final D newData = newD(newLength * 2);
            int newOffset = 0;
            for (int oldOffset = 0; oldOffset < oldLength; oldOffset += oldBlockSize) {
                System.arraycopy(data, oldOffset, newData, newOffset, oldBlockSize);
                newOffset += newBlockSize;
            }
            data = newData;
        } else {
            // Move around the data in the old array
            int newOffset = newLength - newBlockSize;
            for (int oldOffset = oldLength - oldBlockSize; oldOffset > 0; oldOffset -= oldBlockSize) {
                System.arraycopy(data, oldOffset, data, newOffset, oldBlockSize);
                newOffset -= newBlockSize;
            }
            // Don't need to move block 0 around, but has to init the new part of it
            dataFillInit(oldBlockSize, newBlockSize);
        }
    }

    /**
     * @param pos
     * @return the index into data() for the position
     */
    public int pos2ind(final int... pos) {
        int idx = 0;
        int factor = 1;
        for (int dim = 0; dim < pos.length; ++dim) {
            if (pos[dim] > size[dim]) {
                throw new IndexOutOfBoundsException("pos exceeded dimension for dimension " + dim + " (" + pos[dim]
                        + " > " + size[dim] + ")");
            }
            idx += (pos[dim] - 1) * factor;
            factor *= size[dim];
        }
        return idx;
    }

    /**
     * @return the data
     */
    public D getData() {
        return data;
    }

    /**
     * @return the size
     */
    public int[] getSize() {
        return size;
    }

    /**
     * @param i
     *            dimension number in 1 based numbering, 1=row, 2=column
     * @return the size in dimension i
     */
    public int size(final int i) {
        return size[i - 1];
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + Arrays.hashCode(size);
        return result;
    }

    @SuppressWarnings("unchecked")
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
        final AbstractGenericMatrix<D> other = (AbstractGenericMatrix<D>) obj;
        if (!Arrays.equals(size, other.size)) {
            return false;
        }
        if (data == null) {
            if (other.data != null) {
                return false;
            }
        } else if (!(data == other.data || dataEquals(product(size), other.data))) {
            return false;
        }
        return true;
    }

}
