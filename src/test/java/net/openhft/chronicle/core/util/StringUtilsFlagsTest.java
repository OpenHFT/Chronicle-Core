/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Verifies behaviour of StringUtils when reflective optimisation flags are disabled.
 */
public class StringUtilsFlagsTest extends CoreTestCommon {

    private String oldFlag;

    @Before
    public void setUp() {
        oldFlag = System.getProperty("chronicle.core.allow.reflection.string");
        System.setProperty("chronicle.core.allow.reflection.string", "false");
    }

    @After
    public void tearDown() {
        if (oldFlag == null)
            System.clearProperty("chronicle.core.allow.reflection.string");
        else
            System.setProperty("chronicle.core.allow.reflection.string", oldFlag);
    }

    @Test
    public void newStringFallsBackToSafeConstructor() {
        char[] chars = {'C', 'o', 'r', 'e'};
        assertEquals(new String(chars), StringUtils.newString(chars));
    }
}

