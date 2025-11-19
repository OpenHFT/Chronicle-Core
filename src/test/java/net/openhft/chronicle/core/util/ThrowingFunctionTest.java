/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.fail;

public class ThrowingFunctionTest extends CoreTestCommon {
    @Test
    public void asFunction() {
        @NotNull Function<String, String> sc = ThrowingFunction.asFunction(s -> {
            try (@NotNull BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(s), UTF_8))) {
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
