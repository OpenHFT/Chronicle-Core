//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
/**
 * Supplies time providers and utilities for obtaining or setting precise wall-clock timestamps.
 * Implementations range from simple Java wrappers to native-backed providers and tools for
 * unique or user-defined times.
 *
 * <table>
 * <caption>Comparison of time providers</caption>
 * <thead>
 * <tr><th>Provider</th><th>Precision and behaviour</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>{@link SystemTimeProvider}</td><td>Pure Java; synthesises nanoseconds using
 * System.nanoTime and System.currentTimeMillis.</td></tr>
 * <tr><td>{@link PosixTimeProvider}</td><td>Uses native <code>clock_gettime</code> for
 * high resolution.</td></tr>
 * <tr><td>{@link UniqueMicroTimeProvider}</td><td>Ensures unique microsecond timestamps
 * across threads using a delegate provider.</td></tr>
 * <tr><td>{@link SetTimeProvider}</td><td>Manually settable time with optional
 * auto-increment for tests.</td></tr>
 * </tbody>
 * </table>
 *
 * <h2>Quick start</h2>
 * <pre>{@code
 * TimeProvider timeProvider = PosixTimeProvider.INSTANCE;
 * long now = timeProvider.currentTimeNanos();
 * }
 * </pre>
 */
package net.openhft.chronicle.core.time;
