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
package dk.ange.octave.type;

/**
 * Place holder for factory methods
 */
public final class Octave {

    private Octave() {
        throw new UnsupportedOperationException("Do not instantiate");
    }

    /**
     * @param d
     * @return New OctaveDouble with a single value
     */
    public static OctaveDouble scalar(final double d) {
        final OctaveDouble od = new OctaveDouble(1, 1);
        od.set(d, 1, 1);
        return od;
    }

}
