/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Byte buffer cleaning services for different Java versions.
 *
 * <p>This package contains implementations of the
 * {@code ByteBufferCleanerService} SPI that bridge to the underlying
 * JVM specific mechanisms for unmapping or cleaning direct buffers,
 * for example by using {@code Unsafe.invokeCleaner} on Java 9 and
 * newer.
 *
 * <p>The implementations are internal to Chronicle Core and may vary
 * across JDK releases. Callers should use the public cleaner SPI
 * rather than referring to these classes directly.
 */
package net.openhft.chronicle.core.internal.cleaner;
