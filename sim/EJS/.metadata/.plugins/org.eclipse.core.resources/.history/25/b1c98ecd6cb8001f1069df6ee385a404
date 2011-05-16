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
package dk.ange.octave.type.cast;

/**
 * Interface for a caster that transforms objects from type F to T
 * 
 * @param <F>
 * @param <T>
 */
public interface Caster<F, T> {

    /**
     * @param from
     *            object to convert
     * @return Converted object
     */
    public T cast(F from);

    /**
     * @return Class to cast from
     */
    public Class<F> from();

    /**
     * @return Class to cast to
     */
    public Class<T> to();

}
