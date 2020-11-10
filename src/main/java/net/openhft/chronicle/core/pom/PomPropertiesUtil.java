package net.openhft.chronicle.core.pom;

import net.openhft.chronicle.core.internal.pom.InternalPomPropertiesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public final class PomPropertiesUtil {

    private PomPropertiesUtil() {}

    /**
     * Creates and returns a new instance of Properties for the provided {@code libraryName}.
     * <p>
     * The provided {@code libraryName} is used to pick up properties from a resource named
     * {@code /${libraryName}.pom.properties } e.g. "/queue.pom.properties". If no such resource
     * exist, an empty Properties instance is returned.
     *
     * @param libraryName name of the library (e.g. queue)
     * @return a new instance of Properties for the provided {@code libraryName}.
     */
    @NotNull
    public static Properties create(@NotNull final String libraryName) {
        return InternalPomPropertiesUtil.create(libraryName);
    }

    /**
     * Returns the GAV version for the provided {@code libraryName}.
     * <p>
     * The provided {@code libraryName} is used the same way as for
     * {@link #create(String)}.
     *
     * @param libraryName name of the library (e.g. queue)
     * @return the GAV version for the provided {@code libraryName}.
     * 
     * @see #create(String)
     */
    public static String version(@NotNull final String libraryName) {
        return InternalPomPropertiesUtil.version(libraryName);
    }

}