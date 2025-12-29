/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

/**
 * Levels used by Chronicle when reporting operational messages, diagnostics, and runtime health signals.
 *
 * <table>
 *     <caption>Usage and SLF4J mapping</caption>
 *     <tr><th>Level</th><th>When to use</th><th>SLF4J level</th></tr>
 *     <tr><td>{@link #ERROR}</td><td>Something failed and may impact progress.</td><td>{@code error}</td></tr>
 *     <tr><td>{@link #WARN}</td><td>An unexpected condition that does not stop work.</td><td>{@code warn}</td></tr>
 *     <tr><td>{@link #PERF}</td><td>Performance event.</td><td>{@code info}</td></tr>
 *     <tr><td>{@link #DEBUG}</td><td>Diagnostic detail for developers.</td><td>{@code debug}</td></tr>
 * </table>
 *
 * Used by {@link Slf4jExceptionHandler}.
 */
public enum LogLevel {
    /**
     * Error conditions that typically stop progress.
     */
    ERROR,
    /** Unexpected conditions that do not halt execution. */
    WARN,
    /** Performance events or metrics that should be logged at INFO. */
    PERF,
    /** Verbose diagnostic output intended for developers and tooling. */
    DEBUG
}
