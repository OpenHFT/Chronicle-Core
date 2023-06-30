/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

import java.util.List;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;

public class TypeOfTest extends CoreTestCommon {

    @Test
    public <T extends Number> void type() {
        assertEquals("java.util.List<?>",
                new TypeOf<List<?>>() {
                }.type().toString());
        assertEquals("java.util.List<java.lang.String>",
                new TypeOf<List<String>>() {
                }.type().toString());
        assertEquals("java.util.List<T>",
                new TypeOf<List<T>>() {
                }.type().toString());
        assertEquals("java.util.function.BiFunction<java.util.List<java.lang.String>, java.lang.Integer, java.lang.String>",
                new TypeOf<BiFunction<List<String>, Integer, String>>() {
                }.type().toString());
    }
}