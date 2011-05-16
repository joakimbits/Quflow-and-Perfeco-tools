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

import dk.ange.octave.type.matrix.DoubleMatrix;

/**
 * A matrix of doubles
 */
public class OctaveDouble extends DoubleMatrix implements OctaveObject {

    /**
     * Create new matrix
     * 
     * @param size
     */
    public OctaveDouble(final int... size) {
        super(size);
    }

    /**
     * Constructor that reuses the input data
     * 
     * @param data
     * @param size
     */
    public OctaveDouble(final double[] data, final int... size) {
        super(data, size);
    }

    /**
     * Copy constructor
     * 
     * @param o
     */
    public OctaveDouble(final OctaveDouble o) {
        super(o);
    }

    @Override
    public OctaveDouble shallowCopy() {
        return new OctaveDouble(this);
    }

}
