/**
 * Provides classes and interfaces for pooling and efficient access of various resources like classes, strings, and enum values.
 *
 * <p>The {@link net.openhft.chronicle.core.pool.ClassAliasPool} class is responsible for looking up classes
 * and associating them with aliases for more convenient referencing.</p>
 *
 * <p>The {@link net.openhft.chronicle.core.pool.ClassLookup} interface defines contracts for looking up
 * classes by name and associating them with aliases.</p>
 *
 * <p>The {@link net.openhft.chronicle.core.pool.DynamicEnumClass} class represents a dynamic enumeration class
 * that extends the capabilities of {@link net.openhft.chronicle.core.pool.EnumCache} and is capable of dynamically
 * creating and managing instances which resemble enumerations (enums) in behavior.</p>
 *
 * <p>The {@link net.openhft.chronicle.core.pool.EnumCache} class is an abstract base class for caching and efficient
 * access to enum-like instances.</p>
 *
 * <p>The {@link net.openhft.chronicle.core.pool.EnumInterner} class represents a cache for enum values to improve
 * performance in scenarios where the same enum values are frequently looked up by name.</p>
 *
 * <p>The {@link net.openhft.chronicle.core.pool.ParsingCache} class is a cache for parsed values that is optimized for fast lookup.</p>
 *
 * <p>The {@link net.openhft.chronicle.core.pool.StaticEnumClass} class represents a static enumeration class that
 * extends the capabilities of {@link net.openhft.chronicle.core.pool.EnumCache} and is designed to work with traditional
 * Java enum types.</p>
 *
 * <p>The {@link net.openhft.chronicle.core.pool.StringBuilderPool} class provides a pool of StringBuilder objects for
 * efficient string building operations. Each thread gets its own StringBuilder instance via a ThreadLocal, ensuring
 * thread-safety while avoiding synchronization overhead.</p>
 *
 * <p>The {@link net.openhft.chronicle.core.pool.StringInterner} class provides string interning functionality, optimizing
 * memory usage by caching strings and referring to them by index rather than storing duplicate strings.</p>
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
