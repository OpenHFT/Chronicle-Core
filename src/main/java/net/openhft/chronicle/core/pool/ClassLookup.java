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

/**
 * An interface for looking up classes by name and associating them with aliases.
 * This can be useful for reducing verbosity in configurations or serialization,
 * or for dynamically loading classes in a flexible way.
 * <p>
 * Implementations of this interface should handle the logic for aliasing classes
 * and provide a mechanism for looking up classes by either their canonical name
 * or an associated alias.
 */
public interface ClassLookup {

    /**
     * Creates a new ClassLookup instance that wraps this instance. Any changes made
     * to the returned ClassLookup will not affect this instance.
     *
     * @return a new ClassLookup instance wrapping this ClassLookup, ensuring that
     * the underlying instance remains immutable.
     */
    @NotNull
    default ClassLookup wrap() {
        return new ClassAliasPool(this);
    }

    /**
     * Creates a new ClassLookup instance that wraps this instance and uses the provided
     * class loader to look up new classes. Any changes made to the returned ClassLookup
     * will not affect this instance.
     *
     * @param classLoader The ClassLoader used to look up new classes.
     * @return a new ClassLookup instance wrapping this ClassLookup and using the
     * provided ClassLoader, ensuring that the underlying instance remains immutable.
     * @throws NullPointerException if classLoader is null.
     */
    @NotNull
    default ClassLookup wrap(@NotNull ClassLoader classLoader) {
        requireNonNull(classLoader);
        return new ClassAliasPool(this, classLoader);
    }

    /**
     * Looks up and returns the Class object associated with the given class name.
     *
     * @param name The fully qualified name of the desired class.
     * @return The Class object representing the specified class.
     * @throws ClassNotFoundRuntimeException If the class cannot be located.
     */
    Class<?> forName(CharSequence name) throws ClassNotFoundRuntimeException;

    /**
     * Retrieves the alias for the given class. This method is intended for use with
     * non-lambda classes. For lambda classes, this method will throw an IllegalArgumentException.
     *
     * @param clazz The class to retrieve the alias for.
     * @return A String representing the alias for the given class.
     * @throws IllegalArgumentException If this method is used on a lambda function.
     */
    String nameFor(Class<?> clazz) throws IllegalArgumentException;

    /**
     * Adds one or more classes to the class lookup. For each class, its simple name
     * (excluding package) is automatically added as the alias.
     *
     * @param classes The classes to be added to the class lookup.
     */
    void addAlias(Class<?>... classes);

    /**
     * Adds a class to the class lookup with one or more specified aliases. The aliases
     * can be provided as a comma-separated string and may include the package name.
     *
     * @param clazz The class to be added to the class lookup.
     * @param names A single alias or a comma-separated string of aliases for the class.
     */
    void addAlias(Class<?> clazz, String names);
}
