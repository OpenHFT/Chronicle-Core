//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
/**
 * Supplies low level utilities and system helpers used across the Chronicle libraries.
 * The memory model relies on off heap allocations that are managed explicitly and
 * sit outside the JVM garbage collector. See decision log entry CORE-OPS-002 for
 * background on this design.
 */
package net.openhft.chronicle.core;
