/**
 * Provides a service provider interface (SPI) for ByteBuffer cleaning operations.
 *
 * <p>This package contains an interface, {@link net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService},
 * that defines the contract for cleaning memory resources associated with ByteBuffers.
 * This is particularly useful for direct ByteBuffers, where the memory is allocated outside
 * the regular heap and manual intervention is sometimes necessary to release resources.</p>
 *
 * <p>Clients can use different implementations of ByteBufferCleanerService based on their
 * specific needs or constraints. Implementations can be provided by third-party libraries or custom
 * solutions. The impact level of the cleaning operation is also specified, helping clients make informed
 * decisions based on performance or resource considerations.</p>
 *
 * <p>This package is part of the Chronicle Core library, which provides a set of low-level utilities
 * for high-performance systems.</p>
 *
 * @see net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService
 */
package net.openhft.chronicle.core.cleaner.spi;
