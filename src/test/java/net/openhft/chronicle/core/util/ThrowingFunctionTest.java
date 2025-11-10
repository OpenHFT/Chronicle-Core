//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Function;

import static org.junit.Assert.fail;

public class ThrowingFunctionTest extends CoreTestCommon {
    @Test
    public void asFunction() throws Exception {
        @NotNull Function<String, String> sc = ThrowingFunction.asFunction(s -> {
            try (@NotNull BufferedReader br = new BufferedReader(new FileReader(s))) {
                return br.readLine();
            }
        });

        try {
            fail(sc.apply("doesn't exists"));
            if (false) throw new IOException();
        } catch (IOException e) {
            // expected
        }
    }
}
