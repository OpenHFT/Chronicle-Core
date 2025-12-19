/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies behaviour of StringUtils when reflective optimisation flags are disabled.
 */
public class StringUtilsFlagsTest extends CoreTestCommon {

    private String oldFlag;

    @BeforeEach
    public void setUp() {
        oldFlag = System.getProperty("chronicle.core.allow.reflection.string");
        System.setProperty("chronicle.core.allow.reflection.string", "false");
    }

    @AfterEach
    public void tearDown() {
        if (oldFlag == null)
            System.clearProperty("chronicle.core.allow.reflection.string");
        else
            System.setProperty("chronicle.core.allow.reflection.string", oldFlag);
    }

    @Test
    public void newStringFallsBackToSafeConstructor() {
        char[] chars = {'C', 'o', 'r', 'e'};
        assertEquals(new String(chars), StringUtils.newString(chars), "StringUtils.newString should use safe constructor when reflection is disabled");
    }
}

