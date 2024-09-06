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
 * Provides functionality for libraries to announce themselves along with useful information.
 *
 * <p>The {@link net.openhft.chronicle.core.announcer.Announcer} class contains static methods that libraries
 * can use to announce themselves. It prints information related to the artifact and the JVM. This
 * information can include the group ID and artifact ID of the library, and additional properties
 * that provide useful context or data.
 *
 * <p>Announcements can be turned off by setting the system property "chronicle.announcer.disable" to true.
 * This feature might be used for debugging or reducing console output.
 *
 * <p>This package is part of the Chronicle Core library which provides utilities and low-level support
 * for higher-level components.
 *
 * @see net.openhft.chronicle.core.announcer.Announcer
 */
package net.openhft.chronicle.core.announcer;
