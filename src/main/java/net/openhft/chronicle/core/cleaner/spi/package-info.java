/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Service provider interfaces for Chronicle Core cleaning support.
 * <p>
 * Implementations bridge to concrete JVM mechanisms for releasing native buffers and other
 * external resources. Consumers should depend on the abstractions in
 * {@code net.openhft.chronicle.core.cleaner} rather than these SPI types.
 */
package net.openhft.chronicle.core.cleaner.spi;
