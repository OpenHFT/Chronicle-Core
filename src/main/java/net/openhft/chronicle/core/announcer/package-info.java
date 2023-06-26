/**
 * Provides functionality for libraries to announce themselves along with useful information.
 *
 * <p>The {@link net.openhft.chronicle.core.announcer.Announcer} class contains static methods that libraries
 * can use to announce themselves. It prints information related to the artifact and the JVM. This
 * information can include the group ID and artifact ID of the library, and additional properties
 * that provide useful context or data.</p>
 *
 * <p>Announcements can be turned off by setting the system property "chronicle.announcer.disable" to true.
 * This feature might be used for debugging or reducing console output.</p>
 *
 * <p>This package is part of the Chronicle Core library which provides utilities and low-level support
 * for higher-level components.</p>
 *
 * @see net.openhft.chronicle.core.announcer.Announcer
 */
package net.openhft.chronicle.core.announcer;
