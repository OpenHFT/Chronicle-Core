/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Caching and pooling utilities for expensive or frequently reused objects.
 * <p>
 * Typical examples include enum and class lookups, string interners and builder pools used
 * to reduce allocations on hot paths. Callers should treat these as performance helpers and
 * not rely on specific cache eviction behaviour.
 */
package net.openhft.chronicle.core.pool;
