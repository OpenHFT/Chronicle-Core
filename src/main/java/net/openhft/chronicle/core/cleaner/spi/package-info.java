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
 * Provides a service provider interface (SPI) for ByteBuffer cleaning operations.
 *
 * <p>This package contains an interface, {@link net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService},
 * that defines the contract for cleaning memory resources associated with ByteBuffers.
 * This is particularly useful for direct ByteBuffers, where the memory is allocated outside
 * the regular heap and manual intervention is sometimes necessary to release resources.
 *
 * <p>Clients can use different implementations of ByteBufferCleanerService based on their
 * specific needs or constraints. Implementations can be provided by third-party libraries or custom
 * solutions. The impact level of the cleaning operation is also specified, helping clients make informed
 * decisions based on performance or resource considerations.
 *
 * <p>This package is part of the Chronicle Core library, which provides a set of low-level utilities
 * for high-performance systems.
 *
 * @see net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService
 */
package net.openhft.chronicle.core.cleaner.spi;
