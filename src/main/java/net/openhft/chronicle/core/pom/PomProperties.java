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

package net.openhft.chronicle.core.pom;

import net.openhft.chronicle.core.internal.pom.InternalPomProperties;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

/**
 * The {@code PomProperties} class provides utility methods to access Maven POM properties and version information
 * for specified group and artifact IDs. This class is intended for use in environments where project metadata,
 * such as version information, is required at runtime.
 *
 * <p>This class is final and cannot be instantiated or extended. It relies on {@link InternalPomProperties} to
 * retrieve the properties from resources in the format {@code /META-INF/maven/${groupId}/${artifactId}/pom.properties}.
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * Properties properties = PomProperties.create("net.openhft", "chronicle-queue");
 * String version = PomProperties.version("net.openhft", "chronicle-queue");
 * }
 * </pre>
 */
public final class PomProperties {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private PomProperties() {
    }

    /**
     * Creates and returns a new instance of {@link Properties} for the provided parameters.
     *
     * <p>The provided {@code groupId} and {@code artifactId} are used to locate a resource named
     * {@code /META-INF/maven/${groupId}/${artifactId}/pom.properties}. For example,
     * "/META-INF/maven/net.openhft/chronicle-queue/pom.properties". If no such resource exists,
     * an empty {@link Properties} instance is returned.
     *
     * @param groupId    the group ID of the library (e.g., net.openhft)
     * @param artifactId the artifact ID of the library (e.g., chronicle-queue)
     * @return a new instance of {@link Properties} for the provided parameters, or an empty instance if not found.
     */
    @NotNull
    public static Properties create(@NotNull final String groupId, @NotNull final String artifactId) {
        return InternalPomProperties.create(groupId, artifactId);
    }

    /**
     * Returns the GAV (Group, Artifact, Version) version string for the provided parameters,
     * or "unknown" if the version cannot be determined.
     *
     * <p>The provided {@code groupId} and {@code artifactId} are used similarly to the {@link #create(String, String)}
     * method to locate the corresponding POM properties file.
     *
     * @param groupId    the group ID of the library (e.g., net.openhft)
     * @param artifactId the artifact ID of the library (e.g., chronicle-queue)
     * @return the GAV version for the provided parameters, or "unknown" if the version cannot be determined.
     * @see #create(String, String)
     */
    public static String version(@NotNull final String groupId, @NotNull final String artifactId) {
        return InternalPomProperties.version(groupId, artifactId);
    }
}
