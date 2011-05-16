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
package dk.ange.octave.io;

import static dk.ange.octave.io.OctaveIO.readerReadLine;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.Map;

import dk.ange.octave.exception.OctaveParseException;
import dk.ange.octave.exec.ReadFunctor;
import dk.ange.octave.type.OctaveObject;

/**
 * Functor that reads a single variable
 */
final class DataReadFunctor implements ReadFunctor {

    private final String name;

    private OctaveObject data;

    /**
     * @param name
     */
    public DataReadFunctor(final String name) {
        this.name = name;
    }

    public void doReads(final Reader reader) {
        final BufferedReader bufferedReader = new BufferedReader(reader);
        final String createByOctaveLine = readerReadLine(bufferedReader);
        if (createByOctaveLine == null || !createByOctaveLine.startsWith("# Created by Octave")) {
            throw new OctaveParseException("Not created by Octave?: '" + createByOctaveLine + "'");
        }
        final Map<String, OctaveObject> map = OctaveIO.readWithName(bufferedReader);
        if (map.size() != 1) {
            throw new OctaveParseException("Expected map of size 1 but got " + map + "'");
        }
        if (!map.containsKey(name)) {
            throw new OctaveParseException("Expected variable named '" + name + "' but got '" + map + "'");
        }
        data = map.get(name);
    }

    /**
     * @return the data
     */
    public OctaveObject getData() {
        return data;
    }

}
