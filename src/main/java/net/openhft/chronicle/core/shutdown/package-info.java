/**
 * Provides classes and interfaces for handling shutdown procedures in a controlled manner.
 *
 * <p>This package includes classes for registering shutdown hooks with specific priorities, allowing for
 * an orderly shutdown of resources. This is particularly useful in scenarios where resources need to
 * be released or cleaned up in a specific order during the JVM shutdown phase.
 *
 * <p>Example usage:
 * <pre>
 * {@code
 *     // Register a hook to be executed at priority 50.
 *     PriorityHook.add(50, () -> {
 *         // Code to execute during shutdown.
 *     });
 * }
 * </pre>
 *
 * @see net.openhft.chronicle.core.shutdown.Hooklet
 * @see net.openhft.chronicle.core.shutdown.PriorityHook
 *
 * <p>Unless otherwise stated, parameters and return values in this package are
 * non-null by default. Use {@link org.jetbrains.annotations.Nullable} to
 * indicate when {@code null} is permitted.
 */
package net.openhft.chronicle.core.shutdown;
