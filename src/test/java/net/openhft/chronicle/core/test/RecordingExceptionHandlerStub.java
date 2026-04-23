/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.test;

import net.openhft.chronicle.core.onoes.ExceptionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RecordingExceptionHandlerStub implements ExceptionHandler {
    private final List<Event> events = new ArrayList<>();
    private boolean enabled = true;
    private RuntimeException classFailure;
    private RuntimeException loggerFailure;

    @Override
    public void on(@NotNull Class<?> clazz, @Nullable String message, @Nullable Throwable thrown) {
        if (classFailure != null)
            throw classFailure;
        events.add(Event.forClass(clazz, message, thrown));
    }

    @Override
    public void on(@NotNull Logger logger, @Nullable String message, @Nullable Throwable thrown) {
        if (loggerFailure != null)
            throw loggerFailure;
        events.add(Event.forLogger(logger, message, thrown));
    }

    @Override
    public boolean isEnabled(@NotNull Class<?> aClass) {
        return enabled;
    }

    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void throwFromClassHandler(RuntimeException failure) {
        classFailure = failure;
    }

    public void throwFromLoggerHandler(RuntimeException failure) {
        loggerFailure = failure;
    }

    public int eventCount() {
        return events.size();
    }

    public Event event(int index) {
        return events.get(index);
    }

    public List<Event> events() {
        return Collections.unmodifiableList(events);
    }

    public static final class Event {
        private final Class<?> clazz;
        private final Logger logger;
        private final String message;
        private final Throwable thrown;

        private Event(Class<?> clazz, Logger logger, String message, Throwable thrown) {
            this.clazz = clazz;
            this.logger = logger;
            this.message = message;
            this.thrown = thrown;
        }

        private static Event forClass(Class<?> clazz, String message, Throwable thrown) {
            return new Event(clazz, null, message, thrown);
        }

        private static Event forLogger(Logger logger, String message, Throwable thrown) {
            return new Event(null, logger, message, thrown);
        }

        public Class<?> clazz() {
            return clazz;
        }

        public Logger logger() {
            return logger;
        }

        public String message() {
            return message;
        }

        public Throwable thrown() {
            return thrown;
        }
    }
}
