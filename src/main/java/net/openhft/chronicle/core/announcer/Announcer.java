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

package net.openhft.chronicle.core.announcer;

import net.openhft.chronicle.core.internal.announcer.InternalAnnouncer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

/**
 * Provides means for libraries to announce themselves.
 * <p>
 * Announcements can be turned off by setting the system property and provide other information elements.
 * "chronicle.announcer.disable=true" prior to making any announcements.
 */
public final class Announcer {

    public static final String LOGO = "logo";

    private Announcer() {}

    /**
     * Announces the given artifact whereby useful information is printed pertaining
     * to the artifact in particular and the JVM i general.
     * <p>
     * An distinct artifact is only announced once per classloader and general JVM information
     * is only printed once per classloader.
     * <p>
     * Example: net.openhtf:chronicle-queue, net.openhft:chronicle-map and net.openhtf:chronicle-queue (again) is announced.
     * This will produce:
     * <ol>
     *     <li>info on the JVM</li>
     *     <li>info on net.openhtf:chronicle-queue</li>
     *     <li>info on net.openhtf:chronicle-map</li>
     * </ol>
     *
     * @param groupId name of the group (e.g. net.openhft)
     * @param artifactId name of the library (e.g. chronicle-queue)
     */
    public static void announce(@NotNull final String groupId, @NotNull final String artifactId) {
        requireNonNull(groupId);
        requireNonNull(artifactId);
//        InternalAnnouncer.announce(groupId, artifactId, Collections.emptyMap());
    }

    /**
     * Announces the given artifact whereby useful information is printed pertaining
     * to the artifact in particular and the JVM i general accompanied by the provided
     * {@code properties}.
     * <p>
     * The artifact will be announced on each invocation provided that the
     * given {@code properties} is A) not empty or B) only contains a key "logo", for which
     * only one announcement per class loader is made.
     * @see #LOGO
     *
     * @param groupId name of the group (e.g. net.openhft)
     * @param artifactId name of the library (e.g. chronicle-queue)
     * @param properties to print
     */
    public static void announce(@NotNull final String groupId,
                                @NotNull final String artifactId,
                                @NotNull final Map<String, String> properties) {
        requireNonNull(groupId);
        requireNonNull(artifactId);
        requireNonNull(properties);
//        InternalAnnouncer.announce(groupId, artifactId, properties);
    }
}