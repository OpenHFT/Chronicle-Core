/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.init;

import net.openhft.chronicle.core.Jvm;

public final class SystemPropertiesProbeMain {
    private SystemPropertiesProbeMain() {
    }

    public static void main(String[] args) {
        // triggers Jvm class initialisation which loads system.properties
        Jvm.init();
        String value = System.getProperty("chronicle.init.test.flag");
        if (value == null) {
            System.err.println("chronicle.init.test.flag missing");
            System.exit(2);
        }
        System.out.println(value);
    }
}
