/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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

        assertEquals(1, myList.size(), "updateShouldModifyInputAsExpected: L20");
        assertTrue(myList.contains("newElement"), "updateShouldModifyInputAsExpected: L21");
    }

    @Test
    void acceptShouldDelegateToUpdate() {
        Updater<List<String>> appender = list -> list.add("newElement");
        List<String> myList = new ArrayList<>();

        appender.accept(myList); // Using accept instead of update

        assertEquals(1, myList.size(), "acceptShouldDelegateToUpdate: L31");
        assertTrue(myList.contains("newElement"), "acceptShouldDelegateToUpdate: L32");
    }
}
