/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Integrates Chronicle Core with JVM level cleaning mechanisms for off heap resources.
 * <p>
 * Classes in this package locate and abstract the underlying cleaning strategy so that
 * direct buffers and other native allocations can be reclaimed without binding callers
 * to a specific JDK implementation. This package is internal and may change without notice.
 */
package net.openhft.chronicle.core.cleaner;
