//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;

public class JvmMain {
    static {
        System.setProperty("system.properties", "sample.system.properties");
        Jvm.init();
    }

    public static void main(String[] args) {
        Logger isDebug = LoggerFactory.getLogger("isDebug");
        assertTrue(!isDebug.isTraceEnabled() && isDebug.isDebugEnabled());
        Logger isInfo = LoggerFactory.getLogger("isInfo");
        assertTrue(!isInfo.isDebugEnabled() && isInfo.isInfoEnabled());
        Logger isWarn = LoggerFactory.getLogger("isWarn");
        assertTrue(!isWarn.isInfoEnabled() && isWarn.isWarnEnabled());
    }
}
