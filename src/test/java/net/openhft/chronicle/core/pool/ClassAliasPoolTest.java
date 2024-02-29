/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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

import java.time.LocalDate;

import static net.openhft.chronicle.core.pool.ClassAliasPool.CLASS_ALIASES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ClassAliasPoolTest extends CoreTestCommon {


    @Test
    public void testApplyAliasForSet() {
        assertEquals("!set", CLASS_ALIASES.applyAlias("Set").toString());
        assertEquals("!set", CLASS_ALIASES.applyAlias("java.util.Set").toString());
    }

    @Test
    public void testApplyAliasForBitSet() {
        assertEquals("!bitset", CLASS_ALIASES.applyAlias("BitSet").toString());
        assertEquals("!bitset", CLASS_ALIASES.applyAlias("java.util.BitSet").toString());
    }

    @Test
    public void testApplyAliasForSortedSet() {
        assertEquals("!oset", CLASS_ALIASES.applyAlias("SortedSet").toString());
        assertEquals("!oset", CLASS_ALIASES.applyAlias("java.util.SortedSet").toString());
    }

    @Test
    public void testApplyAliasForList() {
        assertEquals("!seq", CLASS_ALIASES.applyAlias("List").toString());
        assertEquals("!seq", CLASS_ALIASES.applyAlias("java.util.List").toString());
    }

    @Test
    public void testApplyAliasForMap() {
        assertEquals("!map", CLASS_ALIASES.applyAlias("Map").toString());
        assertEquals("!map", CLASS_ALIASES.applyAlias("java.util.Map").toString());
    }

    @Test
    public void testApplyAliasForSortedMap() {
        assertEquals("!omap", CLASS_ALIASES.applyAlias("SortedMap").toString());
        assertEquals("!omap", CLASS_ALIASES.applyAlias("java.util.SortedMap").toString());
    }

    @Test
    public void testApplyAliasForString() {
        assertEquals("String", CLASS_ALIASES.applyAlias("java.lang.String").toString());
    }

    @Test
    public void testApplyAliasForByte() {
        assertEquals("byte", CLASS_ALIASES.applyAlias("Byte").toString());
        assertEquals("byte", CLASS_ALIASES.applyAlias("java.lang.Byte").toString());
    }

    @Test
    public void testApplyAliasForInteger() {
        assertEquals("int", CLASS_ALIASES.applyAlias("Integer").toString());
        assertEquals("int", CLASS_ALIASES.applyAlias(Integer.class.getName()).toString());
    }

    @Test
    public void testApplyAliasForLocalDate() {
        assertEquals("Date", CLASS_ALIASES.applyAlias("LocalDate").toString());
        assertEquals("Date", CLASS_ALIASES.applyAlias(LocalDate.class.getName()).toString());
    }

    @Test
    public void forName() {
        CLASS_ALIASES.addAlias(ClassAliasPoolTest.class);
        assertEquals("ClassAliasPoolTest", CLASS_ALIASES.applyAlias(ClassAliasPoolTest.class.getName()));
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

    @Test
    public void banned() {
        for (int i = 0; i < 2; i++) {
            assertThrows(ClassNotFoundRuntimeException.class, () -> CLASS_ALIASES.forName("com.sun.xml.internal.bind.v2.runtime.unmarshaller.Base64Data"));
            assertThrows(ClassNotFoundRuntimeException.class, () -> CLASS_ALIASES.forName("com.sun.istack.internal.ByteArrayDataSource"));
            assertThrows(ClassNotFoundRuntimeException.class, () -> CLASS_ALIASES.forName("com.oracle.webservices.internal.api.databinding.DatabindingFactory"));
            assertThrows(ClassNotFoundRuntimeException.class, () -> CLASS_ALIASES.forName("jdk.internal.util.xml.SAXParser"));
            assertThrows(ClassNotFoundRuntimeException.class, () -> CLASS_ALIASES.forName("sun.corba.SharedSecrets"));
        }
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
