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
 * Provides classes and interfaces for exception handling, logging, and associated utilities.
 * <p>
 * This package contains classes and interfaces that enable handling exceptions
 * in a uniform manner throughout an application. This includes mechanisms for
 * chaining multiple exception handlers together, logging exceptions, handling
 * exceptions on a per-thread basis, and recording exceptions.
 * 
 * <p>
 * The core of this package is the {@link net.openhft.chronicle.core.onoes.ExceptionHandler}
 * interface which allows for custom logic to be defined for handling different
 * types of exceptions.
 * 
 * <p>
 * Other classes and enumerations within the package include:
 * 
 * <ul>
 *     <li>{@link net.openhft.chronicle.core.onoes.ChainedExceptionHandler} - Chains multiple ExceptionHandler objects for sequential invocation.</li>
 *     <li>{@link net.openhft.chronicle.core.onoes.ExceptionKey} - Represents a unique key for an exception event.</li>
 *     <li>{@link net.openhft.chronicle.core.onoes.LogLevel} - Defines various levels of logging severity.</li>
 *     <li>{@link net.openhft.chronicle.core.onoes.NullExceptionHandler} - Implements ExceptionHandler as a null object.</li>
 *     <li>{@link net.openhft.chronicle.core.onoes.PrintExceptionHandler} - Logs exceptions to standard output or error streams.</li>
 *     <li>{@link net.openhft.chronicle.core.onoes.RecordingExceptionHandler} - Records exceptions by incrementing counts in a map.</li>
 *     <li>{@link net.openhft.chronicle.core.onoes.Slf4jExceptionHandler} - Uses SLF4J for logging exceptions based on severity levels.</li>
 *     <li>{@link net.openhft.chronicle.core.onoes.ThreadLocalisedExceptionHandler} - Provides thread-localized exception handling.</li>
 * </ul>
 * <p>
 * This package is part of the Chronicle-Core library by OpenHFT.
 * 
 *
 * @see net.openhft.chronicle.core.onoes.ExceptionHandler
 */
package net.openhft.chronicle.core.onoes;
