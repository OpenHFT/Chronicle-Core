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

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

/**
 * Represents a unique key for an exception event. This key includes the log level,
 * the class where the exception occurred, a message associated with the exception,
 * and the Throwable instance. This key can be used to identify unique exceptions
 * for logging, monitoring, or other purposes.
 * <p>
 * The {@code ExceptionKey} class implements custom {@code equals} and {@code hashCode}
 * methods ensuring that two keys are equal if and only if all their fields are equal.
 * 
 */
public class ExceptionKey {

    /**
     * The log level associated with the exception
     */
    public final LogLevel level;

    /**
     * The class where the exception occurred
     */
    public final Class<?> clazz;

    /**
     * A message associated with the exception
     */
    public final String message;

    /**
     * The Throwable instance representing the exception
     */
    public final Throwable throwable;

    /**
     * Constructs an {@code ExceptionKey} with the specified log level, class,
     * message, and Throwable instance.
     *
     * @param level     The log level associated with the exception.
     * @param clazz     The class where the exception occurred.
     * @param message   A message associated with the exception.
     * @param throwable The Throwable instance representing the exception.
     */
    public ExceptionKey(LogLevel level, Class<?> clazz, String message, Throwable throwable) {
        this.level = level;
        this.clazz = clazz;
        this.message = message;
        this.throwable = throwable;
    }

    /**
     * Returns the log level associated with the exception.
     *
     * @return The log level.
     */
    public LogLevel level() {
        return level;
    }

    /**
     * Returns the class where the exception occurred.
     *
     * @return The class.
     */
    public Class<?> clazz() {
        return clazz;
    }

    /**
     * Returns the message associated with the exception.
     *
     * @return The message.
     */
    public String message() {
        return message;
    }

    /**
     * Returns the Throwable instance representing the exception.
     *
     * @return The throwable.
     */
    public Throwable throwable() {
        return throwable;
    }

    /**
     * Computes the hash code for this exception key based on its fields.
     *
     * @return The hash code value.
     */
    @Override
    public int hashCode() {
        int result = level.hashCode();
        result = 31 * result + clazz.hashCode();
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (throwable != null ? throwable.hashCode() : 0);
        return result;
    }

    /**
     * Determines whether this exception key is equal to another object.
     * Two exception keys are equal if their log level, class, message,
     * and Throwable instance are all equal.
     *
     * @param o The reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExceptionKey that = (ExceptionKey) o;

        if (level != that.level) return false;
        if (!clazz.equals(that.clazz)) return false;
        if (!Objects.equals(message, that.message)) return false;
        return Objects.equals(throwable, that.throwable);
    }

    /**
     * Returns a string representation of the exception key, including the
     * log level, class, message, and the stack trace of the Throwable instance.
     *
     * @return A string representation of the exception key.
     */
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
