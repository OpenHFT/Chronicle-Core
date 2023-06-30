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

package net.openhft.chronicle.core.io;

import junit.framework.TestCase;
import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ReferenceOwnerTest extends CoreTestCommon {
    @Test
    public void testReferenceId() {
        Set<Integer> ints = new HashSet<>();
        for (int i = 0; i < 101; i++)
            ints.add(new VanillaReferenceOwner("hi").referenceId());
        assertEquals(100, ints.size(), 1);
    }

    @Test
    public void testReferenceName() {
        String expectedName = "VanillaReferenceOwner{name='testName'}";
        VanillaReferenceOwner vanillaReferenceOwner = new VanillaReferenceOwner("testName");
        assertEquals(expectedName, vanillaReferenceOwner.referenceName());
    }

    @Test
    public void testToString() {
        String expectedString = "VanillaReferenceOwner{name='anotherTest'}";
        VanillaReferenceOwner vanillaReferenceOwner = new VanillaReferenceOwner("anotherTest");
        assertEquals(expectedString, vanillaReferenceOwner.toString());
    }

    @Test
    public void testIsClosed() {
        VanillaReferenceOwner vanillaReferenceOwner = new VanillaReferenceOwner("closedTest");
        assertFalse(vanillaReferenceOwner.isClosed());
    }
}