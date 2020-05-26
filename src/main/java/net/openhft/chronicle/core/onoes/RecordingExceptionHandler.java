/*
 * Copyright 2016-2020 Chronicle Software
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

import net.openhft.chronicle.core.StackTrace;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RecordingExceptionHandler implements ExceptionHandler {
    private final LogLevel level;
    private final Map<ExceptionKey, Integer> exceptionKeyCountMap;
    private final boolean exceptionsOnly;

    public RecordingExceptionHandler(LogLevel level, Map<ExceptionKey, Integer> exceptionKeyCountMap, boolean exceptionsOnly) {
        this.level = level;
        this.exceptionKeyCountMap = exceptionKeyCountMap;
        this.exceptionsOnly = exceptionsOnly;
    }

    @Override
    public void on(Class clazz, String message, Throwable thrown) {
        if (exceptionsOnly && thrown == null)
            return;
        // assume it is profiling or stack tracing. TODO: do we still need Throwable.class here?
        if (thrown != null && (thrown.getClass() == StackTrace.class || thrown.getClass() == Throwable.class))
            return;
        synchronized (exceptionKeyCountMap) {
            @NotNull ExceptionKey key = new ExceptionKey(level, clazz, message, thrown);
            exceptionKeyCountMap.merge(key, 1, (p, v) -> p == null ? v : p + v);
        }
    }
}
