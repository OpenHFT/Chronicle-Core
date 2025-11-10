//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

/**
 * Levels used by Chronicle when reporting messages.
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
    ERROR,
    WARN,
    PERF,
    DEBUG
}
