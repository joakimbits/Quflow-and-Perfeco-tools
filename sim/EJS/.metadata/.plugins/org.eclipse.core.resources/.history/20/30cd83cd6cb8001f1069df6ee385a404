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
package dk.ange.octave.exception;

import dk.ange.octave.type.OctaveObject;

/**
 * Exception thrown when a cast fails inside JavaOctave
 */
public class OctaveClassCastException extends OctaveRecoverableException {

    private final OctaveObject octaveObject;

    private final Class<? extends OctaveObject> castClass;

    /**
     * @param cause
     *            ClassCastException that we are rethrowing
     * @param octaveObject
     *            OctaveObject that couldn't be casted
     * @param castClass
     *            The class octaveObject couldn't be casted to
     */
    public OctaveClassCastException(final ClassCastException cause, final OctaveObject octaveObject,
            final Class<? extends OctaveObject> castClass) {
        super(cause);
        this.octaveObject = octaveObject;
        this.castClass = castClass;
    }

    /**
     * @return the OctaveObject that couldn't be casted
     */
    public OctaveObject getOctaveObject() {
        return octaveObject;
    }

    /**
     * @return the class octaveObject couldn't be casted to
     */
    public Class<? extends OctaveObject> getCastClass() {
        return castClass;
    }

    @Override
    public String getMessage() {
        final String m1 = "Failed cast of " + octaveObject + " to " + castClass;
        final String m2 = super.getMessage();
        return m1 + (m2 == null ? "" : ", " + m2);
    }

}
