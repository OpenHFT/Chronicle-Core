/**
 * Exception-handling and logging utilities.
 * <p>
 * This package contains classes that enable handling exceptions in a uniform way
 * throughout an application. This includes mechanisms for chaining handlers,
 * logging, per-thread handling and recording exceptions.
 * <p>
 * The core of this package is the {@link net.openhft.chronicle.core.onoes.ExceptionHandler}
 * interface which allows custom logic for dealing with different exception types.
 * <p>
 * Other classes and enumerations within the package include:
 * <ul>
 *     <li>{@link net.openhft.chronicle.core.onoes.ChainedExceptionHandler} - chains multiple ExceptionHandler objects for sequential invocation.</li>
 *     <li>{@link net.openhft.chronicle.core.onoes.ExceptionKey} - represents a unique key for an exception event.</li>
 *     <li>{@link net.openhft.chronicle.core.onoes.LogLevel} - defines levels of logging severity.</li>
 *     <li>{@link net.openhft.chronicle.core.onoes.NullExceptionHandler} - implements ExceptionHandler as a null object.</li>
 *     <li>{@link net.openhft.chronicle.core.onoes.RecordingExceptionHandler} - records exceptions by incrementing counts in a map.</li>
 *     <li>{@link net.openhft.chronicle.core.onoes.Slf4jExceptionHandler} - uses SLF4J for logging based on severity levels.</li>
 *     <li>{@link net.openhft.chronicle.core.onoes.ThreadLocalisedExceptionHandler} - provides thread-localised exception handling.</li>
 * </ul>
 *
 * Example usage for switching to {@link net.openhft.chronicle.core.onoes.RecordingExceptionHandler}:
 * <pre>{@code
 * Map<ExceptionKey, Integer> counts = Jvm.recordExceptions();
 * // run code that generates exceptions
 * Jvm.resetExceptionHandlers();
 * }</pre>
 *
 * @author OpenHFT - part of Chronicle-Core
 * @since 3.25ea
 * @see net.openhft.chronicle.core.onoes.ExceptionHandler
 */
package net.openhft.chronicle.core.onoes;
