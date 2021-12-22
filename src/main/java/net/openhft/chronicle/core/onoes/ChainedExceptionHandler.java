/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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

import net.openhft.chronicle.core.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

public class ChainedExceptionHandler implements ExceptionHandler {
    @NotNull
    private final ExceptionHandler[] chain;

    public ChainedExceptionHandler(@NotNull ExceptionHandler... chain) {
        requireNonNull(chain);
        this.chain = Stream.of(chain)
                .map(ObjectUtils::requireNonNull)
                .toArray(ExceptionHandler[]::new);
    }

    @Override
    public void on(@NotNull Class<?> clazz, @Nullable String message, Throwable thrown) {
        for (ExceptionHandler eh : chain)
            eh.on(clazz, message, thrown);
    }
}
