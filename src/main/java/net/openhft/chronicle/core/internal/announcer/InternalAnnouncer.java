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

package net.openhft.chronicle.core.internal.announcer;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.announcer.Announcer;
import net.openhft.chronicle.core.pom.PomProperties;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class InternalAnnouncer {

    // Suppresses default constructor, ensuring non-instantiability.
    private InternalAnnouncer() {
    }

    private static final boolean DISABLE_ANNOUNCEMENT = Jvm.getBoolean("chronicle.announcer.disable");
    private static final Consumer<String> LINE_PRINTER = DISABLE_ANNOUNCEMENT ? s -> {
    } : m -> Jvm.startup().on(InternalAnnouncer.class, m);
    private static final AtomicBoolean JVM_ANNOUNCED = new AtomicBoolean();
    private static final Map<String, Set<String>> ANNOUNCED_GROUP_IDS = new ConcurrentHashMap<>();

    public static void announce(@NotNull final String groupId,
                                @NotNull final String artifactId,
                                @NotNull final Map<String, String> properties) {
        if (JVM_ANNOUNCED.compareAndSet(false, true)) {
            announceJvm();
        }
        if (alwaysAnnounce(properties)) {
            announceArtifact(groupId, artifactId, properties);
        } else {
            // Only announce once
            final Set<String> announcedArtifacts = ANNOUNCED_GROUP_IDS.computeIfAbsent(groupId, unused -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
            if (announcedArtifacts.add(artifactId)) {
                announceArtifact(groupId, artifactId, properties);
            }
        }
    }

    private static void announceJvm() {
        LINE_PRINTER.accept(String.format("Running under %s %s with %d processors reported.",
                Jvm.getProperty("java.runtime.name", Jvm.getProperty("java.vm.name")),
                Jvm.getProperty("java.runtime.version", Jvm.getProperty("java.vm.version")),
                Runtime.getRuntime().availableProcessors()));
        LINE_PRINTER.accept("Leave your e-mail to get information about the latest releases and patches at https://chronicle.software/release-notes/");
    }

    private static void announceArtifact(@NotNull final String groupId,
                                         @NotNull final String artifactId,
                                         @NotNull final Map<String, String> properties) {
        final Map<String, String> propertiesCopy = new LinkedHashMap<>(properties);
        final String logo = propertiesCopy.remove(Announcer.LOGO);
        if (logo != null) {
            LINE_PRINTER.accept(logo);
        }
        final String version = PomProperties.version(groupId, artifactId);
        final String artifactInfo = String.format("Process id: %d :: %s (%s)", Jvm.getProcessId(), pretty(artifactId), version);
        LINE_PRINTER.accept(artifactInfo);

        final int indent = propertiesCopy.keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);

        final String formatString = "%-" + indent + "s: %s";

        propertiesCopy.entrySet().stream()
                .map(e -> String.format(formatString, e.getKey(), e.getValue()))
                .forEach(LINE_PRINTER);

    }

    // Convert "chronicle-queue" to "Chronicle Queue"
    private static String pretty(@NotNull final String artifactId) {
        final StringBuilder sb = new StringBuilder();
        boolean makeUpperCase = true;
        for (char c : artifactId.toCharArray()) {
            if (makeUpperCase) {
                sb.append(Character.toUpperCase(c));
                makeUpperCase = false;
                continue;
            }
            if ('-' == c) {
                makeUpperCase = true;
                sb.append(' ');
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static boolean alwaysAnnounce(@NotNull final Map<String, String> properties) {
        if (properties.isEmpty())
            return false;
        if (properties.size() == 1)
            // If there is only a "logo" key, then only announce once
            return properties.containsKey("logo");
        return true;
    }

}
