/*
 * Copyright 2016-2020 chronicle.software
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

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.threads.ThreadDump;
import net.openhft.chronicle.core.util.ClassNotFoundRuntimeException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static net.openhft.chronicle.core.pool.ClassAliasPool.CLASS_ALIASES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ClassAliasPoolTest extends CoreTestCommon {

    private ThreadDump threadDump;

    @Before
    public void threadDump() {
        threadDump = new ThreadDump();
    }

    @After
    public void checkThreadDump() {
        threadDump.assertNoNewThreads();
    }

    @Test
    public void forName() {
        CLASS_ALIASES.addAlias(ClassAliasPoolTest.class);
        String simpleName = getClass().getSimpleName();
        assertEquals(ClassAliasPoolTest.class, CLASS_ALIASES.forName(simpleName));
        StringBuilder sb = new StringBuilder(simpleName);
        assertEquals(ClassAliasPoolTest.class, CLASS_ALIASES.forName(sb));
    }

    @Test
    public void testClean() throws IllegalArgumentException {
        assertEquals("String", CLASS_ALIASES.nameFor(String.class));
        CLASS_ALIASES.clean();
        assertEquals("String", CLASS_ALIASES.nameFor(String.class));
    }

    @Test
    public void testEnum() throws IllegalArgumentException {
        assertEquals("net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum",
                CLASS_ALIASES.nameFor(TestEnum.class));
        assertEquals("net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum",
                CLASS_ALIASES.nameFor(TestEnum.FOO.getClass()));
        assertEquals("net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum",
                CLASS_ALIASES.nameFor(TestEnum.BAR.getClass()));
    }

    @Test
    public void replace() {
        expectException("Replaced class net.openhft.chronicle.core.pool.ClassAliasPoolTest with class net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum");
        CLASS_ALIASES.addAlias(ClassAliasPoolTest.class, "name1");
        CLASS_ALIASES.addAlias(TestEnum.class, "name1");
    }

    /**
     * On Windows this would cause a NoClassDefFoundError
     */
    @Test
    public void wrongCaseClassName() {
        assertThrows(ClassNotFoundRuntimeException.class, () -> CLASS_ALIASES.forName(TestEnum.class.getName().toLowerCase()));
    }

    enum TestEnum {
        FOO {
            @Override
            void foo() {
            }
        },
        BAR;

        void foo() {
        }
    }
}