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
 * Provides utility classes and interfaces to efficiently manage the cleanup of byte buffers.
 *
 * <p>This package includes a {@link net.openhft.chronicle.core.cleaner.CleanerServiceLocator}
 * that locates an appropriate implementation of {@link net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService}
 * which can be used to clean DirectByteBuffers. This is especially useful for preventing memory leaks in
 * environments where direct memory is allocated outside of the Java heap.
 *
 * <p>The {@link net.openhft.chronicle.core.cleaner.CleanerServiceLocator} uses the Java ServiceLoader mechanism
 * to find implementations of {@link net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService},
 * which is an interface that can be implemented to provide custom buffer cleaning strategies.
 *
 * @see net.openhft.chronicle.core.cleaner.CleanerServiceLocator
 * @see net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService
 */
package net.openhft.chronicle.core.cleaner;
