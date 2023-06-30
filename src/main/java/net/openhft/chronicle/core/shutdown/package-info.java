/**
 * Provides classes and interfaces for handling shutdown procedures in a controlled manner.
 *
 * <p>This package includes classes for registering shutdown hooks with specific priorities, allowing for
 * an orderly shutdown of resources. This is particularly useful in scenarios where resources need to
 * be released or cleaned up in a specific order during the JVM shutdown phase.</p>
 *
 * <p>Example usage:</p>
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
 */
package net.openhft.chronicle.core.shutdown;
