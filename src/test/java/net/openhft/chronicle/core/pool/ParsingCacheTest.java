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

package net.openhft.chronicle.core.pool;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class ParsingCacheTest {
    @Test
    public void intern() throws Exception {
        @NotNull ParsingCache<BigDecimal> pc = new ParsingCache<>(128, BigDecimal::new);
        @Nullable BigDecimal bd1 = pc.intern("1.234");
        @Nullable BigDecimal bd2 = pc.intern("12.234");
        @Nullable BigDecimal bd1b = pc.intern("1.234");
        assertNotEquals(bd1, bd2);
        assertSame(bd1, bd1b);
        assertEquals(2, pc.valueCount());
    }
}