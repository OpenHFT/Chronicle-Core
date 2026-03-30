/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.util.ClassNotFoundRuntimeException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static net.openhft.chronicle.core.pool.ClassAliasPool.CLASS_ALIASES;
import static org.junit.jupiter.api.Assertions.*;

class ClassAliasPoolTest extends CoreTestCommon {

    @Test
    void testApplyAliasForSet() {
        assertEquals("!set", CLASS_ALIASES.applyAlias("Set").toString());
        assertEquals("!set", CLASS_ALIASES.applyAlias("java.util.Set").toString());
    }

    @Test
    void testApplyAliasForBitSet() {
        assertEquals("!bitset", CLASS_ALIASES.applyAlias("BitSet").toString());
        assertEquals("!bitset", CLASS_ALIASES.applyAlias("java.util.BitSet").toString());
    }

    @Test
    void testApplyAliasForSortedSet() {
        assertEquals("!oset", CLASS_ALIASES.applyAlias("SortedSet").toString());
        assertEquals("!oset", CLASS_ALIASES.applyAlias("java.util.SortedSet").toString());
    }

    @Test
    void testApplyAliasForList() {
        assertEquals("!seq", CLASS_ALIASES.applyAlias("List").toString());
        assertEquals("!seq", CLASS_ALIASES.applyAlias("java.util.List").toString());
    }

    @Test
    void testApplyAliasForMap() {
        assertEquals("!map", CLASS_ALIASES.applyAlias("Map").toString());
        assertEquals("!map", CLASS_ALIASES.applyAlias("java.util.Map").toString());
    }

    @Test
    void testApplyAliasForSortedMap() {
        assertEquals("!omap", CLASS_ALIASES.applyAlias("SortedMap").toString());
        assertEquals("!omap", CLASS_ALIASES.applyAlias("java.util.SortedMap").toString());
    }

    @Test
    void testApplyAliasForString() {
        assertEquals("String", CLASS_ALIASES.applyAlias("java.lang.String").toString());
    }

    @Test
    void testApplyAliasForByte() {
        assertEquals("byte", CLASS_ALIASES.applyAlias("Byte").toString());
        assertEquals("byte", CLASS_ALIASES.applyAlias("java.lang.Byte").toString());
    }

    @Test
    void testApplyAliasForInteger() {
        assertEquals("int", CLASS_ALIASES.applyAlias("Integer").toString());
        assertEquals("int", CLASS_ALIASES.applyAlias(Integer.class.getName()).toString());
    }

    @Test
    void testApplyAliasForLocalDate() {
        assertEquals("Date", CLASS_ALIASES.applyAlias("LocalDate").toString());
        assertEquals("Date", CLASS_ALIASES.applyAlias(LocalDate.class.getName()).toString());
    }

    @Test
    void forName() {
        CLASS_ALIASES.addAlias(ClassAliasPoolTest.class);
        assertEquals("ClassAliasPoolTest", CLASS_ALIASES.applyAlias(ClassAliasPoolTest.class.getName()));
        String simpleName = getClass().getSimpleName();
        assertSame(ClassAliasPoolTest.class, ClassAliasPool.CLASS_ALIASES.forName(simpleName));
        StringBuilder sb = new StringBuilder(simpleName);
        assertSame(ClassAliasPoolTest.class, ClassAliasPool.CLASS_ALIASES.forName(sb));
    }

    @Test
    void testClean() throws IllegalArgumentException {
        assertEquals("String", CLASS_ALIASES.nameFor(String.class));
        CLASS_ALIASES.clean();
        assertEquals("String", CLASS_ALIASES.nameFor(String.class));
    }

    @Test
    void testEnum() throws IllegalArgumentException {
        assertEquals("net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum",
                CLASS_ALIASES.nameFor(TestEnum.class));
        assertEquals("net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum",
                CLASS_ALIASES.nameFor(TestEnum.FOO.getClass()));
        assertEquals("net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum",
                CLASS_ALIASES.nameFor(TestEnum.BAR.getClass()));
    }

    @Test
    void replace() {
        expectException("Replaced class net.openhft.chronicle.core.pool.ClassAliasPoolTest with class net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum");
        CLASS_ALIASES.addAlias(ClassAliasPoolTest.class, "name1");
        CLASS_ALIASES.addAlias(TestEnum.class, "name1");
    }

    /**
     * On Windows this would cause a NoClassDefFoundError
     */
    @Test
    void wrongCaseClassName() {
        assertThrows(ClassNotFoundRuntimeException.class, () -> CLASS_ALIASES.forName(TestEnum.class.getName().toLowerCase()));
    }

    @Test
    void banned() {
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

        @SuppressWarnings({"EmptyMethod", "unused"})
        void foo() {
            // No-op: placeholder method for enum constant overriding behaviour in tests
        }
    }
}
