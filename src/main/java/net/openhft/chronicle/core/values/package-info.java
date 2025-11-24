/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Abstractions for mutable scalar and array values.
 * <p>
 * The interfaces in this package model references to primitive and
 * string values that may be stored on-heap, off-heap or in
 * memory-mapped files. They are used across Chronicle components for
 * counters, flags and configuration that must be updated with minimal
 * allocation.
 * <p>
 * Implementations typically support concurrent access with explicit
 * semantics for volatile reads, ordered writes and compare-and-swap
 * operations. Many types form part of the public API surface and are
 * referenced from higher-level Chronicle modules; implementations may
 * evolve but the behavioural contracts expressed in the interfaces are
 * kept stable.
 */
package net.openhft.chronicle.core.values;
