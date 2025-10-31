/*
 * Copyright 2016-2025 chronicle.software
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
/**
 * Tools to manage resource lifecycles.
 *
 * <p>This package formalises how resources are acquired, shared and eventually
 * released. Implementations typically extend
 * {@link net.openhft.chronicle.core.io.AbstractCloseable} or use
 * {@link net.openhft.chronicle.core.io.ReferenceCounted} to guard access with
 * reference counts. Thread-safety is enforced by optional checks and
 * {@link net.openhft.chronicle.core.io.BackgroundResourceReleaser} can offload
 * clean up work to a background thread.</p>
 *
 * <h2>Features</h2>
 * <ul>
 *     <li>Abstract base classes for close operations with single-threaded or
 *     multi-threaded safety guarantees.</li>
 *     <li>Reference counting to ensure resources are released exactly once.</li>
 *     <li>Background releasing via
 *     {@link net.openhft.chronicle.core.io.BackgroundResourceReleaser}.</li>
 *     <li>Interfaces for querying the closeable state of an object.</li>
 *     <li>Validation hooks for serialization or deserialization.</li>
 *     <li>Custom exceptions for illegal state caused by resource closure.</li>
 *     <li>Tracking and monitoring of reference counts and resource owners.</li>
 * </ul>
 *
 * <h2>Key Classes and Interfaces</h2>
 * <ul>
 *     <li>{@link net.openhft.chronicle.core.io.AbstractCloseable} &ndash; base
 *     class for deterministic closeable resources.</li>
 *     <li>{@link net.openhft.chronicle.core.io.BackgroundResourceReleaser}
 *     &ndash; assists releasing reference counted resources off the critical
 *     path.</li>
 *     <li>{@link net.openhft.chronicle.core.io.ReferenceCounted} &ndash;
 *     interface for resources that track active users.</li>
 *     <li>{@link net.openhft.chronicle.core.io.AbstractCloseableReferenceCounted}
 *     &ndash; convenience base integrating both closing and reference counting.</li>
 *     <li>{@link net.openhft.chronicle.core.io.Closeable}
 *     &ndash; simple interface for a closeable destination or source.</li>
 *     <li>{@link net.openhft.chronicle.core.io.ReferenceOwner} &ndash; identifies
 *     an owner of a reservation.</li>
 *     <li>{@link net.openhft.chronicle.core.io.ManagedCloseable}
 *     &ndash; expert closeable API offering more control.</li>
 *     <li>{@link net.openhft.chronicle.core.io.Validatable} &ndash; allows state
 *     checks before using method writers.</li>
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
 * <h2>Example</h2>
 * <pre>{@code
 * try (MyResource resource = new MyResource()) {
 *     resource.reserve(this);
 *     try {
 *         // use the resource
 *     } finally {
 *         resource.release(this);
 *     }
 * }
 * }</pre>
 *
 * @see net.openhft.chronicle.core.io.Closeable
 * @see net.openhft.chronicle.core.io.ManagedCloseable
 */
package net.openhft.chronicle.core.io;
