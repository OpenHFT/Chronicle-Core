/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ThrowingFunctionTest extends CoreTestCommon {
    @Test
    @DisplayName("asFunction rethrows IO failures from lambda")
    void asFunction() {
        @NotNull Function<String, String> sc = ThrowingFunction.asFunction(s -> {
            try (@NotNull BufferedReader br = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(Paths.get(s)), UTF_8))) {
                return br.readLine();
            }
        });

        assertThrows(IOException.class, () -> sc.apply("doesn't exists"),
                "asFunction should rethrow I/O failures");
    }
}
