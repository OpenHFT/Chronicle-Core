/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Chronicle Core threading and event loop abstractions.
 * <p>
 * The package defines event loops, event handlers, timer utilities and
 * helper threads used to schedule and execute non-blocking tasks in
 * Chronicle components. Typical usage follows a single-writer model:
 * each {@code EventLoop} runs on a dedicated thread and invokes
 * handlers in priority order.
 * <p>
 * Selected types in this package form part of the public API surface
 * for building low-latency services; other classes are supporting
 * utilities and may change between releases. Implementations are
 * long-lived resources that should be stopped and closed explicitly
 * during service shutdown.
 */
package net.openhft.chronicle.core.threads;
