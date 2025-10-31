/*
 * Copyright 2016-2025 chronicle.software
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

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UpdaterTest {

    @Test
    void updateShouldModifyInputAsExpected() {
        Updater<List<String>> appender = list -> list.add("newElement");
        List<String> myList = new ArrayList<>();

        appender.update(myList);

        assertEquals(1, myList.size());
        assertTrue(myList.contains("newElement"));
    }

    @Test
    void acceptShouldDelegateToUpdate() {
        Updater<List<String>> appender = list -> list.add("newElement");
        List<String> myList = new ArrayList<>();

        appender.accept(myList); // Using accept instead of update

        assertEquals(1, myList.size());
        assertTrue(myList.contains("newElement"));
    }
}
