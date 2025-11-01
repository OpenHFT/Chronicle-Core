/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.util.IgnoresEverything;
import net.openhft.chronicle.core.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

/**
 * Chains exception handlers.
 * <p>
 * Handlers are evaluated left to right. If a handler throws, the failure is
 * logged at error level and the next handler is called.
 * <p>
 * Construction prunes instances of {@link IgnoresEverything} and unwraps any
 * {@link ThreadLocalisedExceptionHandler}.
 *
 * <p> The chain is immutable and has no internal synchronisation. It is
 * thread-safe provided the supplied handlers are thread-safe.
 *
 * <pre>
 * ExceptionHandler chain = new ChainedExceptionHandler(
 *     Slf4jExceptionHandler.ERROR,
 *     new RecordingExceptionHandler(LogLevel.ERROR, map, true)
 * );
 * </pre>
 */
public class ChainedExceptionHandler implements ExceptionHandler {
    @NotNull
    private final ExceptionHandler[] chain;

    /**
     * Creates a new chain of handlers.
     *
     * @param chain the handlers to evaluate from left to right
     * @throws NullPointerException if {@code chain} or any element is null
     */
    public ChainedExceptionHandler(@NotNull ExceptionHandler... chain) {
        requireNonNull(chain);
        this.chain = Stream.of(chain)
                .filter(e -> !(e instanceof IgnoresEverything))
                .map(ObjectUtils::requireNonNull)
                .map(ThreadLocalisedExceptionHandler::unwrap)
                .toArray(ExceptionHandler[]::new);
    }

    /**
     * Passes the event to each handler.
     *
     * @param clazz   the originating class, not null
     * @param message an optional message
     * @param thrown  an optional throwable
     * @throws NullPointerException if {@code clazz} is null
     */
    @Override
    public void on(@NotNull Class<?> clazz, @Nullable String message, @Nullable Throwable thrown) {
        for (ExceptionHandler eh : chain) {
            try {
                eh.on(clazz, message, thrown);
            } catch (Throwable t) {
                LoggerFactory.getLogger(eh.getClass()).error("Unable to call with message " + message, t);
            }
        }
    }

    /**
     * Passes the event to each handler.
     *
     * @param logger  the logger to use, not null
     * @param message an optional message
     * @param thrown  an optional throwable
     * @throws NullPointerException if {@code logger} is null
     */
    @Override
    public void on(@NotNull Logger logger, @Nullable String message, Throwable thrown) {
        for (ExceptionHandler eh : chain)
            try {
                eh.on(logger, message, thrown);
            } catch (Throwable t) {
                LoggerFactory.getLogger(eh.getClass()).error("Unable to call with message " + message, t);
            }
    }

    /**
     * Returns the handlers in evaluation order.
     *
     * @return the immutable handler array
     */
    public @NotNull ExceptionHandler[] chain() {
        return chain;
    }
}
