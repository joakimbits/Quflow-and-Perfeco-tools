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
 * General matrix with boolean values in
 */
public class BooleanMatrix extends AbstractGenericMatrix<boolean[]> {

    /**
     * @param size
     */
    protected BooleanMatrix(final int... size) {
        super(size);
    }

    /**
     * @param data
     * @param size
     */
    protected BooleanMatrix(final boolean[] data, final int... size) {
        super(data, size);
    }

    @Override
    protected boolean[] newD(final int size_) {
        return new boolean[size_];
    }

    @Override
    protected int dataLength() {
        return data.length;
    }

    @Override
    protected void dataFillInit(final int fromIndex, final int toIndex) {
        Arrays.fill(data, fromIndex, toIndex, false);
    }

    @Override
    protected boolean dataEquals(final int usedLength, final boolean[] otherData) {
        for (int i = 0; i < usedLength; i++) {
            if (data[i] != otherData[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Set the value
     * 
     * @param value
     * @param pos
     */
    public void set(final boolean value, final int... pos) {
        resizeUp(pos);
        data[pos2ind(pos)] = value;
    }

    /**
     * Get the value
     * 
     * @param pos
     * @return value at pos
     */
    public boolean get(final int... pos) {
        return data[pos2ind(pos)];
    }

}
