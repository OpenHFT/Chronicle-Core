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

package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.io.InvalidMarshallableException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class VanillaEventHandlerGptTest {

    @Test
    public void testAction() {
        TestEventHandler handler = new TestEventHandler(5);

        // Test that action returns true while there is more work to be done
        try {
            for (int i = 0; i < 5; i++) {
                assertEquals(i < 4, handler.action());
                assertEquals(i + 1, handler.getCounter());
            }
        } catch (InvalidEventHandlerException | InvalidMarshallableException e) {
            fail("Exception should not have been thrown");
        }

        // Test that on the 5th iteration, InvalidEventHandlerException is thrown
        try {
            handler.action();
            fail("Expected InvalidEventHandlerException to be thrown");
        } catch (InvalidEventHandlerException e) {
            // expected
            assertEquals(5, handler.getCounter());
        } catch (InvalidMarshallableException e) {
            fail("InvalidMarshallableException should not have been thrown");
        }
    }
}
