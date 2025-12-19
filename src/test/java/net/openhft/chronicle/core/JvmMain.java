/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JvmMain {
    static {
        System.setProperty("system.properties", "sample.system.properties");
        Jvm.init();
    }

    public static void main(String[] args) {
        Logger isDebug = LoggerFactory.getLogger("isDebug");
        assertTrue(!isDebug.isTraceEnabled() && isDebug.isDebugEnabled(), "isDebug logger should have debug level without trace");
        Logger isInfo = LoggerFactory.getLogger("isInfo");
        assertTrue(!isInfo.isDebugEnabled() && isInfo.isInfoEnabled(), "isInfo logger should have info level without debug");
        Logger isWarn = LoggerFactory.getLogger("isWarn");
        assertTrue(!isWarn.isInfoEnabled() && isWarn.isWarnEnabled(), "isWarn logger should have warn level without info");
    }
}
