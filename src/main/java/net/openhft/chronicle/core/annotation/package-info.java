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
 * Provides annotations for specifying and documenting constraints, expected behavior, and
 * meta-information for elements within the Chronicle Core library.
 * <p>
 * The annotations in this package can be used for a range of purposes such as:
 * <ul>
 *     <li>Specifying expected value ranges or conditions for variables and parameters.</li>
 *     <li>Documenting intended usage scenarios, such as single-threaded access.</li>
 *     <li>Conveying information about the design decisions, such as package-local access.</li>
 *     <li>Indicating implications of modifying elements referred to by clients or accessed through reflection.</li>
 * </ul>
 * <p>
 * Examples of annotations provided in this package include:
 * <ul>
 *     <li>{@link net.openhft.chronicle.core.annotation.Negative}</li>
 *     <li>{@link net.openhft.chronicle.core.annotation.NonNegative}</li>
 *     <li>{@link net.openhft.chronicle.core.annotation.SingleThreaded}</li>
 *     <li>{@link net.openhft.chronicle.core.annotation.PackageLocal}</li>
 * </ul>
 * <p>
 * These annotations may be used to convey intentions, constraints, and additional information
 * about elements in the codebase which can be beneficial for documentation, analysis or runtime behavior.
 */
package net.openhft.chronicle.core.annotation;
