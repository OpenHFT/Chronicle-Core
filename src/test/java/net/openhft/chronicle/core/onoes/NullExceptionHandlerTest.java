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

package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Mocker;
import net.openhft.chronicle.core.util.IgnoresEverything;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class NullExceptionHandlerTest extends CoreTestCommon {
    @Test
    public void ignoresEverything() {
        assertTrue(ExceptionHandler.ignoresEverything() instanceof IgnoresEverything);
    }

    @Test
    public void ignoresEverything2() {
        assertTrue(Mocker.ignored(ExceptionHandler.class) instanceof IgnoresEverything);
    }
}