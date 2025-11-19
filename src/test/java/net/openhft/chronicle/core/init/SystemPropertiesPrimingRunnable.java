/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.init;

import net.openhft.chronicle.core.Jvm;

import static org.junit.Assert.assertNotNull;

public class SystemPropertiesPrimingRunnable implements Runnable {
    @Override
    public void run() {
        String path = System.getProperty("chronicle.init.test.systemPropertiesPath");
        assertNotNull("chronicle.init.test.systemPropertiesPath must be provided", path);
        System.setProperty(Jvm.SYSTEM_PROPERTIES, path);
    }
}

