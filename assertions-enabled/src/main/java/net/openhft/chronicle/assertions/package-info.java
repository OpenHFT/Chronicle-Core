/**
 * Assertion utilities with assertions enabled.
 *
 * <p>This variant of the {@code net.openhft.chronicle.assertions}
 * package keeps assertion checks active so that invariant violations
 * are detected during development, testing, or diagnostic runs.
 *
 * <p>The package is intended to be selected via build profiles rather
 * than referenced directly. Application code should only depend on
 * the public surface of {@code AssertUtil}.
 */
package net.openhft.chronicle.assertions;
