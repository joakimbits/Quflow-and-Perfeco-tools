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

import dk.ange.octave.type.matrix.BooleanMatrix;

/**
 * A Boolean matrix
 */
public class OctaveBoolean extends BooleanMatrix implements OctaveObject {

    /**
     * @param size
     */
    public OctaveBoolean(final int... size) {
        super(size);
    }

    /**
     * @param data
     * @param size
     */
    public OctaveBoolean(final boolean[] data, final int... size) {
        super(data, size);
    }

    @Override
    public OctaveBoolean shallowCopy() {
        return new OctaveBoolean(data, size);
    }

}
