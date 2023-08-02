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
import net.openhft.chronicle.core.Mocker;
import org.junit.Test;

import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;

public class IgnoresEverythingTest extends CoreTestCommon {
    @Test
    public void test() {
        assertTrue(Mocker.ignored(Consumer.class) instanceof IgnoresEverything);
    }

    @Test
    public void returnsIgnored() {
        assertTrue(Mocker.ignored(Chained.class).method1() instanceof IgnoresEverything);
    }

    interface Chained {
        Chained2 method1();
    }

    interface Chained2 {
        Object method2();
    }
}