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

import net.openhft.chronicle.core.threads.ThreadDump;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static net.openhft.chronicle.core.pool.ClassAliasPool.CLASS_ALIASES;
import static org.junit.Assert.assertEquals;

public class ClassAliasPoolTest {

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
    public void forName() throws ClassNotFoundException {
        CLASS_ALIASES.addAlias(ClassAliasPoolTest.class);
        String simpleName = getClass().getSimpleName();
        assertEquals(ClassAliasPoolTest.class, CLASS_ALIASES.forName(simpleName));
        StringBuilder sb = new StringBuilder(simpleName);
        assertEquals(ClassAliasPoolTest.class, CLASS_ALIASES.forName(sb));
    }

    @Test
    public void testClean() {
        assertEquals("String", CLASS_ALIASES.nameFor(String.class));
        CLASS_ALIASES.clean();
        assertEquals("String", CLASS_ALIASES.nameFor(String.class));
    }

    @Test
    public void testEnum() {
        assertEquals("net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum",
                CLASS_ALIASES.nameFor(TestEnum.class));
        assertEquals("net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum",
                CLASS_ALIASES.nameFor(TestEnum.FOO.getClass()));
        assertEquals("net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum",
                CLASS_ALIASES.nameFor(TestEnum.BAR.getClass()));
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