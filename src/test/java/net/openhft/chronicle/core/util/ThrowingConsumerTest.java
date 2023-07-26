/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.Assert.fail;

public class ThrowingConsumerTest extends CoreTestCommon {
    @Test
    public void asConsumer() throws Exception {
        @NotNull Consumer<String> sc = ThrowingConsumer.asConsumer(s -> {
            try (@NotNull BufferedReader br = new BufferedReader(new FileReader(s))) {
                System.out.println(br.readLine());
            }
        });

        try {
            sc.accept("doesn't exists");
            fail();
            if (false) throw new IOException();
        } catch (IOException e) {
            // expected
        }
    }
}