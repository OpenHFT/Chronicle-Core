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

package net.openhft.chronicle.core.pool;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StringBuilderPoolGptTest {

    @Test
    public void acquireStringBuilderTest() {
        // Initialize the StringBuilderPool
        StringBuilderPool pool = new StringBuilderPool();

        // Acquire a StringBuilder and append a string
        StringBuilder sb = pool.acquireStringBuilder();
        sb.append("Hello, World!");

        // Verify that the StringBuilder contains the correct string
        assertEquals("Hello, World!", sb.toString());

        // Acquire the same StringBuilder again (since it's the same thread)
        StringBuilder sb2 = pool.acquireStringBuilder();

        // Since the StringBuilder is reset upon acquisition, the StringBuilder from the pool should be empty
        assertEquals(0, sb2.length());

        // Confirm that both StringBuilder variables are pointing to the same instance
        assertTrue(sb == sb2);
    }
}
