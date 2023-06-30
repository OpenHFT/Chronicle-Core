/**
 * Provides utility classes and interfaces to efficiently manage the cleanup of byte buffers.
 *
 * <p>This package includes a {@link net.openhft.chronicle.core.cleaner.CleanerServiceLocator}
 * that locates an appropriate implementation of {@link net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService}
 * which can be used to clean DirectByteBuffers. This is especially useful for preventing memory leaks in
 * environments where direct memory is allocated outside of the Java heap.</p>
 *
 * <p>The {@link net.openhft.chronicle.core.cleaner.CleanerServiceLocator} uses the Java ServiceLoader mechanism
 * to find implementations of {@link net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService},
 * which is an interface that can be implemented to provide custom buffer cleaning strategies.</p>
 *
 * @see net.openhft.chronicle.core.cleaner.CleanerServiceLocator
 * @see net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService
 */
package net.openhft.chronicle.core.cleaner;
