/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Provides classes and interfaces for pooling and efficient access of various resources like classes, strings, and enum values.
 *
 * <p>The {@link net.openhft.chronicle.core.pool.ClassAliasPool} class is responsible for looking up classes
 * and associating them with aliases for more convenient referencing.
 *
 * <p>The {@link net.openhft.chronicle.core.pool.ClassLookup} interface defines contracts for looking up
 * classes by name and associating them with aliases.
 *
 * <p>The {@link net.openhft.chronicle.core.pool.DynamicEnumClass} class represents a dynamic enumeration class
 * that extends the capabilities of {@link net.openhft.chronicle.core.pool.EnumCache} and is capable of dynamically
 * creating and managing instances which resemble enumerations (enums) in behavior.
 *
 * <p>The {@link net.openhft.chronicle.core.pool.EnumCache} class is an abstract base class for caching and efficient
 * access to enum-like instances.
 *
 * <p>The {@link net.openhft.chronicle.core.pool.EnumInterner} class represents a cache for enum values to improve
 * performance in scenarios where the same enum values are frequently looked up by name.
 *
 * <p>The {@link net.openhft.chronicle.core.pool.ParsingCache} class is a cache for parsed values that is optimized for fast lookup.
 *
 * <p>The {@link net.openhft.chronicle.core.pool.StaticEnumClass} class represents a static enumeration class that
 * extends the capabilities of {@link net.openhft.chronicle.core.pool.EnumCache} and is designed to work with traditional
 * Java enum types.
 *
 * <p>The {@link net.openhft.chronicle.core.pool.StringBuilderPool} class provides a pool of StringBuilder objects for
 * efficient string building operations. Each thread gets its own StringBuilder instance via a ThreadLocal, ensuring
 * thread-safety while avoiding synchronization overhead.
 *
 * <p>The {@link net.openhft.chronicle.core.pool.StringInterner} class provides string interning functionality, optimizing
 * memory usage by caching strings and referring to them by index rather than storing duplicate strings.
 *
 * @see net.openhft.chronicle.core.pool.ClassAliasPool
 * @see net.openhft.chronicle.core.pool.ClassLookup
 * @see net.openhft.chronicle.core.pool.DynamicEnumClass
 * @see net.openhft.chronicle.core.pool.EnumCache
 * @see net.openhft.chronicle.core.pool.EnumInterner
 * @see net.openhft.chronicle.core.pool.ParsingCache
 * @see net.openhft.chronicle.core.pool.StaticEnumClass
 * @see net.openhft.chronicle.core.pool.StringBuilderPool
 * @see net.openhft.chronicle.core.pool.StringInterner
 */
package net.openhft.chronicle.core.pool;
