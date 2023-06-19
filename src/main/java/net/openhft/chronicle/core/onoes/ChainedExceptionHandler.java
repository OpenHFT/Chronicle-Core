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
 *
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
 * in the chain will be called.</p>
 *
 * <p>When constructing a new instance, all {@code ExceptionHandler}s that are instances of
 * {@link IgnoresEverything} will be filtered out. Furthermore, any {@code ExceptionHandler} that
 * is an instance of {@link ThreadLocalisedExceptionHandler} will be unwrapped to its underlying
 * {@code ExceptionHandler}.</p>
 */
public class ChainedExceptionHandler implements ExceptionHandler {
    @NotNull
    private final ExceptionHandler[] chain;

    public ChainedExceptionHandler(@NotNull ExceptionHandler... chain) {
        requireNonNull(chain);
        this.chain = Stream.of(chain)
                .filter(e -> !(e instanceof IgnoresEverything))
                .map(ObjectUtils::requireNonNull)
                .map(ThreadLocalisedExceptionHandler::unwrap)
                .toArray(ExceptionHandler[]::new);
    }

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

    @Override
    public void on(@NotNull Logger logger, @Nullable String message, Throwable thrown) {
        for (ExceptionHandler eh : chain)
            try {
                eh.on(logger, message, thrown);
            } catch (Throwable t) {
                LoggerFactory.getLogger(eh.getClass()).error("Unable to call with message " + message, t);
            }
    }

    public @NotNull ExceptionHandler[] chain() {
        return chain;
    }
}
