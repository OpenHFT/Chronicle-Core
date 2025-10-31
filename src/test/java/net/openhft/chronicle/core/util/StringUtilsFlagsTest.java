/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

