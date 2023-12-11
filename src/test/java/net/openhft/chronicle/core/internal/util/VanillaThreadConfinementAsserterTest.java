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

package net.openhft.chronicle.core.internal.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.util.ThreadConfinementAsserter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class VanillaThreadConfinementAsserterTest extends CoreTestCommon {

    private ThreadConfinementAsserter asserter;

    @Before
    public void before() {
        asserter = new VanillaThreadConfinementAsserter();
    }

    @Test
    public void assertThreadConfinedSame() {
        asserter.assertThreadConfined();
    }

    @Test
    public void assertThreadConfinedOther() throws InterruptedException {
        final Thread other = new Thread(() -> asserter.assertThreadConfined(), "first");
        other.start();
        other.join();

        // The asserter is now touched by another thread
        assertThrows(IllegalStateException.class, () ->
                asserter.assertThreadConfined()
        );
    }

}
