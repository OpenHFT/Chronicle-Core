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
 * The resource management package provides classes and interfaces for managing the lifecycle of
 * resources such as files, streams, and memory buffers. It includes abstractions for reference counting,
 * closing resources, and utilities for background resource release.
 *
 * <h2>Features</h2>
 * <ul>
 *     <li>Abstract base classes for resources requiring close operations with thread-safety guarantees.</li>
 *     <li>Reference counting capabilities for efficient resource management.</li>
 *     <li>Utilities for managing reference counted resources in the background.</li>
 *     <li>Interfaces for querying the closeable state of an object.</li>
 *     <li>Interfaces for validating objects before serialization or deserialization.</li>
 *     <li>Custom exceptions for indicating illegal state due to resource closure.</li>
 *     <li>Support for tracking and monitoring reference counts and resource owners.</li>
 *     <li>Support for single-threaded access checking.</li>
 * </ul>
 *
 * <h2>Key Classes and Interfaces</h2>
 * <ul>
 *     <li>{@link net.openhft.chronicle.core.io.AbstractCloseable} - Base class for closeable resources with additional utilities for managing the resource lifecycle.</li>
 *     <li>{@link net.openhft.chronicle.core.io.AbstractCloseableReferenceCounted} - Represents a closeable resource with reference counting capabilities.</li>
 *     <li>{@link net.openhft.chronicle.core.io.BackgroundResourceReleaser} - Utility class for managing reference counted resources and related operations in the background.</li>
 *     <li>{@link net.openhft.chronicle.core.io.Closeable} - Interface for a source or destination of data that can be closed.</li>
 *     <li>{@link net.openhft.chronicle.core.io.ReferenceOwner} - Represents an entity that owns a reference.</li>
 *     <li>{@link net.openhft.chronicle.core.io.ManagedCloseable} - Extends the Closeable interface providing additional methods for expert use cases involving resource lifecycle management.</li>
 *     <li>{@link net.openhft.chronicle.core.io.ReferenceCounted} - Represents a resource that is reference counted.</li>
 *     <li>{@link net.openhft.chronicle.core.io.Validatable} - Interface for objects that require validation of their state before being written through a method writer.</li>
 * </ul>
 *
 * <h2>Custom Exceptions</h2>
 * <ul>
 *     <li>{@link net.openhft.chronicle.core.io.ClosedIllegalStateException} - Indicating that a method has been invoked on a closed resource.</li>
 *     <li>{@link net.openhft.chronicle.core.io.ClosedIORuntimeException} - Indicating that an I/O operation has been attempted on a closed I/O resource.</li>
 *     <li>{@link net.openhft.chronicle.core.io.InvalidMarshallableException} - Indicating that an object being serialized or deserialized is in an invalid state.</li>
 *     <li>{@link net.openhft.chronicle.core.io.IORuntimeException} - A runtime exception thrown when an operation involving an underlying IO resource fails.</li>
 * </ul>
 *
 * <h2>Use Cases</h2>
 * This package can be used in scenarios where there is a need to manage the lifecycle of resources,
 * especially those requiring closing operations. Examples include:
 * <ul>
 *     <li>File handling systems.</li>
 *     <li>Network connections management.</li>
 *     <li>Memory buffer management.</li>
 *     <li>Database connections pooling.</li>
 * </ul>
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * try (CloseableResource resource = new CloseableResource()) {
 *     // use the resource
 * } catch (IORuntimeException e) {
 *     // handle exception
 * }
 * }</pre>
 *
 * @see net.openhft.chronicle.core.io.Closeable
 * @see net.openhft.chronicle.core.io.ManagedCloseable
 */
package net.openhft.chronicle.core.io;