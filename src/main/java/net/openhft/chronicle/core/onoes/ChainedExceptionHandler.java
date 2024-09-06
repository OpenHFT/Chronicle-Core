/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * An implementation of {@link ExceptionHandler} that chains multiple {@link ExceptionHandler} objects
 * to be invoked in sequence.
 *
 * <p>This class encapsulates an ordered collection of {@code ExceptionHandler} instances and calls
 * each of them in turn when an exception occurs. If an {@code ExceptionHandler} in the chain
 * itself throws an exception, this exception will be logged and the next {@code ExceptionHandler}
 * in the chain will be called.
 *
 * <p>When constructing a new instance, all {@code ExceptionHandler}s that are instances of
 * {@link IgnoresEverything} will be filtered out. Furthermore, any {@code ExceptionHandler} that
 * is an instance of {@link ThreadLocalisedExceptionHandler} will be unwrapped to its underlying
 * {@code ExceptionHandler}.
 *
 * <p>This design allows for flexible and robust exception handling by chaining together multiple
 * handlers that can perform different actions in response to exceptions.
 */
public class ChainedExceptionHandler implements ExceptionHandler {

    /**
     * An array of {@code ExceptionHandler} objects that form the chain of handlers.
     */
    @NotNull
    private final ExceptionHandler[] chain;

    /**
     * Constructs a new {@code ChainedExceptionHandler} with the specified array of {@code ExceptionHandler} instances.
     *
     * <p>Each provided {@code ExceptionHandler} will be filtered and unwrapped as follows:
     * <ul>
     *     <li>Instances of {@link IgnoresEverything} are excluded from the chain.</li>
     *     <li>Instances of {@link ThreadLocalisedExceptionHandler} are unwrapped to obtain their underlying {@code ExceptionHandler}.</li>
     * </ul>
     *
     * @param chain the array of {@code ExceptionHandler} instances to chain together.
     * @throws NullPointerException if the {@code chain} array or any of its elements are {@code null}.
     */
    public ChainedExceptionHandler(@NotNull ExceptionHandler... chain) {
        // Ensure the input array is not null.
        requireNonNull(chain);

        // Filter and unwrap the exception handlers, then store them in the chain.
        this.chain = Stream.of(chain)
                .filter(e -> !(e instanceof IgnoresEverything))
                .map(ObjectUtils::requireNonNull)
                .map(ThreadLocalisedExceptionHandler::unwrap)
                .toArray(ExceptionHandler[]::new);
    }

    /**
     * Handles an exception by invoking each {@code ExceptionHandler} in the chain with the provided class, message, and throwable.
     *
     * <p>If any {@code ExceptionHandler} in the chain throws an exception during its execution, the exception is logged,
     * and the next handler in the chain is called.
     *
     * @param clazz   the class where the exception occurred.
     * @param message the exception message, which can be {@code null}.
     * @param thrown  the {@code Throwable} object representing the exception, which can be {@code null}.
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
     * Handles an exception by invoking each {@code ExceptionHandler} in the chain with the provided logger, message, and throwable.
     *
     * <p>If any {@code ExceptionHandler} in the chain throws an exception during its execution, the exception is logged,
     * and the next handler in the chain is called.
     *
     * @param logger  the {@code Logger} used for logging the exception.
     * @param message the exception message, which can be {@code null}.
     * @param thrown  the {@code Throwable} object representing the exception, which can be {@code null}.
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
     * Returns the array of {@code ExceptionHandler} objects that form the chain.
     *
     * @return the array of {@code ExceptionHandler} objects in the chain.
     */
    @NotNull
    public ExceptionHandler[] chain() {
        return chain;
    }
}
