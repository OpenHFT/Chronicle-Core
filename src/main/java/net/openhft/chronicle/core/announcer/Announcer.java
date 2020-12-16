package net.openhft.chronicle.core.announcer;

import net.openhft.chronicle.core.internal.announcer.InternalAnnouncer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

/**
 * Provides means for libraries to announce themselves.
 * <p>
 * Announcements can be turned off buy setting the system property
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
        InternalAnnouncer.announce(groupId, artifactId, Collections.emptyMap());
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
        InternalAnnouncer.announce(groupId, artifactId, properties);
    }


}