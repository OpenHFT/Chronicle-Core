/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.assertions.AssertUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ZeroCostAssertionStatusTest extends CoreTestCommon {

    @DisplayName("show behaviour under expected input and output conditions")
    @Test
    void show() {
        boolean ae = false;
        try {
            assert 0 != 0;
        } catch (AssertionError assertionError) {
            ae = true;
        }

        boolean zcae = false;
        try {
            assert AssertUtil.SKIP_ASSERTIONS;
        } catch (AssertionError assertionError) {
            zcae = true;
        }

        System.out.println("Normal assertions are " + (ae ? "ON" : "OFF"));
        System.out.println("Zero-cost assertions are " + (zcae ? "ON" : "OFF"));
        assertTrue(true, "execution should reach this point without exception"); // if we reach here, the test passes
    }
}
