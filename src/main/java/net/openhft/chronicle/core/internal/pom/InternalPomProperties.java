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

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for loading Maven POM properties such as version information for a given artifact.
 * <p>
 * This class reads the `pom.properties` file from the classpath, typically stored in `/META-INF/maven/groupId/artifactId/pom.properties`,
 * and extracts properties like the version. The extracted information is cached for efficiency in future lookups.
 * </p>
 * <p>
 * This class is not intended to be instantiated and provides only static utility methods.
 * </p>
 */
public final class InternalPomProperties {

    // Suppresses default constructor, ensuring non-instantiability.
    private InternalPomProperties() {
    }

    // Cache for storing artifact version information to avoid repeated lookups
    private static final Map<String, String> VERSION_CACHE = new ConcurrentHashMap<>();

    /**
     * Creates and loads a {@link Properties} object for the specified groupId and artifactId by locating the `pom.properties` file.
     * <p>
     * The method looks for the `pom.properties` file located at `/META-INF/maven/{groupId}/{artifactId}/pom.properties`.
     * If found, the properties are loaded and returned. If the file is not found or an error occurs, an empty set of properties is returned.
     * </p>
     *
     * @param groupId    The Maven group ID of the artifact.
     * @param artifactId The Maven artifact ID.
     * @return A {@link Properties} object containing the properties from the `pom.properties` file, or an empty properties object if not found.
     */
    @NotNull
    public static Properties create(@NotNull final String groupId, @NotNull final String artifactId) {
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

    /**
     * Retrieves the version of the specified Maven artifact from the `pom.properties` file.
     * <p>
     * This method uses a cache to store and retrieve the version information efficiently. If the version is not already cached,
     * it loads the `pom.properties` file, extracts the version, and caches the result. If the version cannot be found, "unknown" is returned.
     * </p>
     *
     * @param groupId    The Maven group ID of the artifact.
     * @param artifactId The Maven artifact ID.
     * @return The version of the artifact, or "unknown" if the version cannot be determined.
     */
    public static String version(@NotNull final String groupId, @NotNull final String artifactId) {
        return VERSION_CACHE.computeIfAbsent(groupId + ":" + artifactId, unused -> InternalPomProperties.extractVersionOrUnknown(groupId, artifactId));
    }

    /**
     * Constructs the path to the `pom.properties` file for the specified Maven artifact.
     * <p>
     * The path follows the structure `/META-INF/maven/{groupId}/{artifactId}/pom.properties`.
     * </p>
     *
     * @param groupId    The Maven group ID of the artifact.
     * @param artifactId The Maven artifact ID.
     * @return The path to the `pom.properties` file as a string.
     */
    private static String resourceName(@NotNull final String groupId, @NotNull final String artifactId) {
        return "/META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties";
    }

    /**
     * Extracts the version of the specified Maven artifact from the `pom.properties` file.
     * <p>
     * If the version cannot be found, this method returns "unknown".
     * </p>
     *
     * @param groupId    The Maven group ID of the artifact.
     * @param artifactId The Maven artifact ID.
     * @return The version of the artifact, or "unknown" if the version is not found.
     */
    private static String extractVersionOrUnknown(@NotNull final String groupId, @NotNull final String artifactId) {
        return create(groupId, artifactId).getProperty("version", "unknown");
    }
}
