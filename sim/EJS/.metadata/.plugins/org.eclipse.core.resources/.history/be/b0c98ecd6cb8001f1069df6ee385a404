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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.spi.ServiceRegistry;

import dk.ange.octave.exception.OctaveClassCastException;
import dk.ange.octave.type.OctaveObject;

/**
 * Helper class for the auto cast functionality
 */
public final class Cast {

    private static Map<ClassPair<?, ?>, Caster<?, ?>> casterMap;

    private Cast() {
        throw new UnsupportedOperationException("Do not instantiate");
    }

    @SuppressWarnings("unchecked")
    private static synchronized void initIfNecessary() {
        if (casterMap == null) {
            casterMap = new HashMap<ClassPair<?, ?>, Caster<?, ?>>();
            final Iterator<Caster> sp = ServiceRegistry.lookupProviders(Caster.class);
            while (sp.hasNext()) {
                register(sp.next());
            }
        }
    }

    private static <F, T> void register(final Caster<F, T> caster) {
        final ClassPair<F, T> cp = new ClassPair<F, T>(caster.from(), caster.to());
        if (casterMap.containsKey(cp)) {
            throw new RuntimeException("casterMap.containsKey(cp)");
        }
        casterMap.put(cp, caster);
    }

    /**
     * Cast and transform the object
     * 
     * @param <F>
     * @param <T>
     * @param toClass
     * @param from
     * @return The transformed object
     */
    public static <F extends OctaveObject, T extends OctaveObject> T cast(final Class<T> toClass, final F from) {
        if (from == null) {
            return null;
        }
        if (toClass.isInstance(from)) {
            return toClass.cast(from);
        }
        final ClassPair<F, T> cp = new ClassPair<F, T>(unsafeGetClass(from), toClass);
        final Caster<F, T> caster = casterMapGet(cp);
        if (caster == null) {
            throw new OctaveClassCastException(null, from, toClass);
        }
        return caster.cast(from);
    }

    @SuppressWarnings("unchecked")
    private static <F> Class<F> unsafeGetClass(final F from) {
        return (Class<F>) from.getClass();
    }

    @SuppressWarnings("unchecked")
    private static <F extends OctaveObject, T extends OctaveObject> Caster<F, T> casterMapGet(final ClassPair<F, T> cp) {
        initIfNecessary();
        if (!casterMap.containsKey(cp)) {
            return null;
        }
        final Caster<F, T> caster = (Caster<F, T>) casterMap.get(cp);
        if (!caster.from().equals(cp.from)) {
            throw new RuntimeException("!caster.from().equals(cp.from)");
        }
        if (!caster.to().equals(cp.to)) {
            throw new RuntimeException("!caster.to().equals(cp.to)");
        }
        return caster;
    }

    private static class ClassPair<F, T> {
        ClassPair(final Class<F> from, final Class<T> to) {
            this.from = from;
            this.to = to;
        }

        Class<F> from;

        Class<T> to;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((from == null) ? 0 : from.hashCode());
            result = prime * result + ((to == null) ? 0 : to.hashCode());
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
            final ClassPair other = (ClassPair) obj;
            if (from == null) {
                if (other.from != null) {
                    return false;
                }
            } else if (!from.equals(other.from)) {
                return false;
            }
            if (to == null) {
                if (other.to != null) {
                    return false;
                }
            } else if (!to.equals(other.to)) {
                return false;
            }
            return true;
        }
    }

}
