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
import dk.ange.octave.type.OctaveFake;

/**
 * The writer of all {@link OctaveFake} objects
 */
public final class FakeWriter extends OctaveDataWriter<OctaveFake> {

    @Override
    public Class<OctaveFake> javaType() {
        return OctaveFake.class;
    }

    @Override
    public void write(final Writer writer, final OctaveFake octaveFake) throws IOException {
        writer.write(octaveFake.getData());
    }

}
