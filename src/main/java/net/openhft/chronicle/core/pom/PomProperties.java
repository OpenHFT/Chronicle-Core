package net.openhft.chronicle.core.pom;

import net.openhft.chronicle.core.internal.pom.InternalPomProperties;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public final class PomProperties {

    private PomProperties() {}

    /**
     * Creates and returns a new instance of Properties for the provided parameters.
     * <p>
     * The provided {@code libraryName} is used to pick up properties from a resource named
     * {@code /META-INF/maven/${groupId}/${artifactId}/pom.properties }
     * e.g. "/META-INF/maven/net.openhft/chronicle-queue/pom.properties". If no such resource
     * exist, an empty Properties instance is returned.
     *
      * @param groupId name of the group (e.g. net.openhft)
      * @param artifactId name of the library (e.g. chronicle-queue)
     * @return a new instance of Properties for the provided parameters.
     */
    @NotNull
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
     * @param groupId name of the group (e.g. net.openhft)
     * @param artifactId name of the library (e.g. chronicle-queue)
     * @return the GAV version for the provided parameters, or "unknown" if
     *         the version cannot be determined
     * 
     * @see #create(String, String)
     */
    public static String version(@NotNull final String groupId, @NotNull final String artifactId) {
        return InternalPomProperties.version(groupId, artifactId);
    }
}