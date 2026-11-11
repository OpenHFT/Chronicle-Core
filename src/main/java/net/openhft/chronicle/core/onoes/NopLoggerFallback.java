/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;
import org.slf4j.helpers.NOPLoggerFactory;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;

/** No event, class or throwable is retained by the fallback. */
final class NopLoggerFallback {
    private static final AtomicBoolean ANNOUNCED = new AtomicBoolean();

    private NopLoggerFallback() {
    }

    static boolean enabled(Logger logger) {
        // Do not call Jvm here: its own initialisation installs these handlers.
        return !disabled()
                && logger instanceof NOPLogger
                && LoggerFactory.getILoggerFactory() instanceof NOPLoggerFactory;
    }

    private static boolean disabled() {
        // Match Jvm.getBoolean(key, false) without triggering Jvm/ObjectUtils initialisation.
        String value = System.getProperty("chronicle.logging.disableStderrFallback");
        if (value == null)
            return false;
        if (value.isEmpty())
            return true;
        value = value.trim();
        return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)
                || "t".equalsIgnoreCase(value) || "y".equalsIgnoreCase(value);
    }

    static void write(String level, String source, String message, Throwable thrown) {
        PrintStream out = System.err;
        // Keep each event and its stack trace together, including concurrent callers.
        synchronized (out) {
            if (ANNOUNCED.compareAndSet(false, true))
                out.println("Chronicle stderr fallback enabled: SLF4J is using a NOP logger factory; " +
                        "install a provider or set -Dchronicle.logging.disableStderrFallback=true to stop this fallback.");
            out.println("[" + level + "] " + source + " - " + String.valueOf(message));
            if (thrown != null)
                thrown.printStackTrace(out);
        }
    }
}
