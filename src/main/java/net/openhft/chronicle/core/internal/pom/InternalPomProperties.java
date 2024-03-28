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

package net.openhft.chronicle.core.internal.pom;

import net.openhft.chronicle.core.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public final class InternalPomProperties {

    // Suppresses default constructor, ensuring non-instantiability.
    private InternalPomProperties() {
    }

    private static final Map<String, String> VERSION_CACHE = new ConcurrentHashMap<>();

    @NotNull
    public static Properties create(@NotNull final String groupId, @NotNull final String artifactId) {
        ObjectUtils.requireNonNull(groupId);
        ObjectUtils.requireNonNull(artifactId);
        final Properties properties = new Properties();
        try {
            final String resourceName = resourceName(groupId, artifactId);
            try (InputStream inputStream = InternalPomProperties.class.getResourceAsStream(resourceName)) {
                if (inputStream != null) {
                    properties.load(inputStream);
                }
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
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
