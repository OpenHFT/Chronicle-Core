/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.pom;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public final class InternalPomProperties {

    // Suppresses default constructor, ensuring non-instantiability.
    private InternalPomProperties() {
    }

    // CSUnboundedInternCache keep assuming the number of groupId:artifactId are low
    private static final Map<String, String> VERSION_CACHE = new ConcurrentHashMap<>();

    @NotNull
    public static Properties create(final String groupId, final String artifactId) {
        ObjectUtils.requireNonNull(groupId);
        ObjectUtils.requireNonNull(artifactId);
        final Properties properties = new Properties();
        final String resourceName = resourceName(groupId, artifactId);
        try {
            try (InputStream inputStream = InternalPomProperties.class.getResourceAsStream(resourceName)) {
                if (inputStream != null) {
                    properties.load(inputStream);
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            Jvm.debug().on(InternalPomProperties.class, "Error reading " + resourceName, e);
            // Returns an empty set of properties if we fail.
        }
        return properties;
    }

    public static String version(@NotNull final String groupId, @NotNull final String artifactId) {
        return VERSION_CACHE.computeIfAbsent(groupId + ":" + artifactId, unused -> InternalPomProperties.extractVersionOrUnknown(groupId, artifactId));
    }

    private static String resourceName(@NotNull final String groupId, @NotNull final String artifactId) {
        return "/META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties";
    }

    private static String extractVersionOrUnknown(@NotNull final String groupId, @NotNull final String artifactId) {
        return create(groupId, artifactId).getProperty("version", "unknown");
    }
}
