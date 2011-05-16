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
 * A function handle.
 * <p>
 * This does for some reason not work with octave 3.0, but it works with octave 3.2.
 */
public class OctaveFunctionHandle implements OctaveObject {

    private final String function;

    /**
     * @param function
     *            A single line string like "@(x) x ** 2" or "@(a,b) a + b"
     */
    public OctaveFunctionHandle(final String function) {
        this.function = function;
    }

    @Override
    public OctaveFunctionHandle shallowCopy() {
        return new OctaveFunctionHandle(getFunction());
    }

    /**
     * @return the function
     */
    public String getFunction() {
        return function;
    }

}
