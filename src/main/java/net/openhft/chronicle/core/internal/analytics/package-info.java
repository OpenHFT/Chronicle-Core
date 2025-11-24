/**
 * Internal plumbing for optional analytics integration.
 *
 * <p>This package provides utility classes that build standard maps
 * of event parameters and user properties and, when enabled, forward
 * those events to a pluggable analytics back end by reflection.
 *
 * <p>The design is intentionally conservative: analytics is disabled
 * by default, and the implementation may change or be removed without
 * notice. No public API or compatibility guarantees are offered for
 * these types.
 */
package net.openhft.chronicle.core.internal.analytics;

