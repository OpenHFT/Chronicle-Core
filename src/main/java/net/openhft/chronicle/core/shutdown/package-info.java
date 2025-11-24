/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Ordering and coordination of application shutdown logic.
 * <p>
 * This package defines hook registration and prioritisation so that Chronicle based services
 * can stop cleanly, close resources in a predictable order and minimise data loss on exit.
 */
package net.openhft.chronicle.core.shutdown;
