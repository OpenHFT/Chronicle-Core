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

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionKey {
    public final LogLevel level;
    public final Class clazz;
    public final String message;
    public final Throwable throwable;

    public ExceptionKey(LogLevel level, Class clazz, String message, Throwable throwable) {
        this.level = level;
        this.clazz = clazz;
        this.message = message;
        this.throwable = throwable;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        return obj == this || toString().equals(obj.toString());
    }

    @NotNull
    @Override
    public String toString() {
        @NotNull StringWriter sw = new StringWriter();
        if (throwable != null)
            throwable.printStackTrace(new PrintWriter(sw));
        return "ExceptionKey{" +
                "level=" + level +
                ", clazz=" + clazz +
                ", message='" + message + '\'' +
                ", throwable=" + sw +
                '}';
    }
}
