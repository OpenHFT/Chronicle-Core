/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Function;

import static org.junit.Assert.fail;

public class ThrowingFunctionTest {
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