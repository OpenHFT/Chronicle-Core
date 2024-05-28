/**
 * Provides classes and interfaces for thread management and scheduling in an
 * event-driven programming model. The classes in this package offer functionality
 * for scheduling tasks to run periodically, managing thread-local variables, and
 * handling events using an event loop.
 *
 * <p>Key classes and interfaces:
 * <ul>
 *     <li>{@link net.openhft.chronicle.core.threads.CancellableTimer}: A timer that can
 *     schedule tasks for periodic execution or execution after a delay.</li>
 *
 *     <li>{@link net.openhft.chronicle.core.threads.CleaningThread}: Extends the Thread
 *     class to clean up thread-local variables when the thread completes its execution.</li>
 *
 *     <li>{@link net.openhft.chronicle.core.threads.CleaningThreadLocal}: Extends
 *     ThreadLocal and ensures that resources held by a CleaningThread are cleaned
 *     up when the thread dies.</li>
 *
 *     <li>{@link net.openhft.chronicle.core.threads.DelegatingEventLoop}: An implementation
 *     of EventLoop that delegates calls to an underlying EventLoop instance. Useful as a base
 *     class for custom implementations.</li>
 *
 *     <li>{@link net.openhft.chronicle.core.threads.EventHandler}: Interface representing
 *     a handler for events within an event loop.</li>
 *
 *     <li>{@link net.openhft.chronicle.core.threads.EventLoop}: Represents an event-driven
 *     loop responsible for processing event handlers based on their priority.</li>
 *
 *     <li>{@link net.openhft.chronicle.core.threads.HandlerPriority}: Enum representing
 *     different priority levels for event handlers in an event loop.</li>
 *
 *     <li>{@link net.openhft.chronicle.core.threads.InterruptedRuntimeException}: A runtime
 *     exception representing the interruption of a thread.</li>
 *
 *     <li>{@link net.openhft.chronicle.core.threads.InvalidEventHandlerException}: Represents
 *     an exception thrown when an event handler is invalid or needs to be removed.</li>
 *
 *     <li>{@link net.openhft.chronicle.core.threads.OnDemandEventLoop}: A wrapper for an
 *     EventLoop, which is created on-demand when any of its methods are called.</li>
 *
 *     <li>{@link net.openhft.chronicle.core.threads.ThreadDump}: Utility class for monitoring
 *     and managing threads.</li>
 *
 *     <li>{@link net.openhft.chronicle.core.threads.ThreadLocalHelper}: A utility class for
 *     managing values in a ThreadLocal.</li>
 *
 *     <li>{@link net.openhft.chronicle.core.threads.Timer}: A timer used to schedule tasks
 *     for periodic execution or execution after a delay.</li>
 *
 *     <li>{@link net.openhft.chronicle.core.threads.VanillaEventHandler}: Represents an event
 *     handler that performs actions within an event loop.</li>
 * </ul>
 *
 * @see java.lang.Thread
 * @see java.lang.ThreadLocal
 */
package net.openhft.chronicle.core.threads;
