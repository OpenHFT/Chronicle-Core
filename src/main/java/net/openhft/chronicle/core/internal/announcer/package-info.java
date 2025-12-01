/**
 * Internal support for Chronicle startup announcements.
 *
 * <p>Types in this package implement the logic that prints JVM and
 * artefact information at process start up, including optional logos
 * and version details, routed via {@code Jvm.startup()}.
 *
 * <p>The behaviour is controlled by system properties and is
 * considered an internal diagnostic facility rather than part of the
 * public Chronicle Core API.
 */
package net.openhft.chronicle.core.internal.announcer;
