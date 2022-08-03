/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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

package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.util.ClassNotFoundRuntimeException;
import org.jetbrains.annotations.NotNull;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

public interface ClassLookup {

    /**
     * Wrap this class into
     *
     * @return a ClassLookup which can be modified without changing the underlying ClassLookup
     */
    @NotNull
    default ClassLookup wrap() {
        return new ClassAliasPool(this);
    }

    /**
     * Creates and returns a new ClassLookup wrapping this ClassLookup and using the
     * provided {@code classLoader} to look up new classes.
     *
     * @return a new ClassLookup which can be modified without changing the underlying ClassLookup
     * that is using a custom ClassLoader
     */
    @NotNull
    default ClassLookup wrap(@NotNull ClassLoader classLoader) {
        requireNonNull(classLoader);
        return new ClassAliasPool(this, classLoader);
    }

    /**
     * Turn a string into a class
     *
     * @param name of the type/class
     * @return the fclass for that name
     * @throws ClassNotFoundRuntimeException
     */
    Class<?> forName(CharSequence name) throws ClassNotFoundRuntimeException;

    /**
     * @param clazz to lookup an alias for
     * @return the alias
     * @throws IllegalArgumentException if used on a lambda function.
     */
    String nameFor(Class<?> clazz) throws IllegalArgumentException;

    /**
     * Add classes to the class lookup. The simple name without the package is added as the alias
     *
     * @param classes to add
     */
    void addAlias(Class<?>... classes);

    /**
     * Add a class with a specific alias which may or may not have a package name.
     *
     * @param clazz to alias
     * @param names An alias or comma seperated list of aliases
     */
    void addAlias(Class<?> clazz, String names);
}
