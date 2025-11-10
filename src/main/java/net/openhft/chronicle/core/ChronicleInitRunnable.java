//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

public interface ChronicleInitRunnable extends Runnable {
    /**
     * This method will be run once at the end of Jvm.class static initialization.
     */
    default void postInit() {
        // No-op.
    }
}
