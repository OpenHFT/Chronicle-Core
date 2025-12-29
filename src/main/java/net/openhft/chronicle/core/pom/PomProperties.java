/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pom;

import net.openhft.chronicle.core.internal.pom.InternalPomProperties;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

/**
 * Access to Maven build metadata at runtime for diagnostics, reporting, and version inspection.
 *
 * <p>It loads the {@code pom.properties} resource for a given
 * {@code groupId} and {@code artifactId}, allowing callers to
 * inspect version and other build-time information.</p>
 *
 * <p>All methods require non-null parameters. Passing {@code null}
 * results in a {@link NullPointerException}.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * Properties props = PomProperties.create("net.openhft", "chronicle-core");
 * String version = props.getProperty("version");
 * }</pre>
 */
public final class PomProperties {

    private PomProperties() {
    }

    /**
     * Creates and returns a new instance of Properties for the provided parameters.
     * <p>
     * The provided {@code libraryName} is used to pick up properties from a resource named
     * {@code /META-INF/maven/${groupId}/${artifactId}/pom.properties }
     * e.g. "/META-INF/maven/net.openhft/chronicle-queue/pom.properties". If no such resource
     * exist, an empty Properties instance is returned.
     *
     * @param groupId    name of the group (e.g. net.openhft)
     * @param artifactId name of the library (e.g. chronicle-queue)
     * @return a new instance of Properties for the provided parameters.
     */
    @NotNull
    @Deprecated(/* to be removed in 2027, only used in tests */)
    public static Properties create(@NotNull final String groupId, @NotNull final String artifactId) {
        return InternalPomProperties.create(groupId, artifactId);
    }

    /**
     * Returns the GAV version for the provided parameters, or "unknown" if
     * the version cannot be determined.
     * <p>
     * The provided {@code libraryName} is used the same way as for
     * {@link #create(String, String)}.
     *
     * @param groupId    name of the group (e.g. net.openhft)
     * @param artifactId name of the library (e.g. chronicle-queue)
     * @return the GAV version for the provided parameters, or "unknown" if
     * the version cannot be determined
     * @see #create(String, String)
     */
    public static String version(@NotNull final String groupId, @NotNull final String artifactId) {
        return InternalPomProperties.version(groupId, artifactId);
    }
}
