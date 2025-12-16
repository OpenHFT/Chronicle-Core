/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.util.ClassNotFoundRuntimeException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static net.openhft.chronicle.core.pool.ClassAliasPool.CLASS_ALIASES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ClassAliasPoolTest extends CoreTestCommon {

    @Test
    public void testApplyAliasForSet() {
        assertEquals("!set", CLASS_ALIASES.applyAlias("Set").toString(), "testApplyAliasForSet: L20");
        assertEquals("!set", CLASS_ALIASES.applyAlias("java.util.Set").toString(), "testApplyAliasForSet: L21");
    }

    @Test
    public void testApplyAliasForBitSet() {
        assertEquals("!bitset", CLASS_ALIASES.applyAlias("BitSet").toString(), "testApplyAliasForBitSet: L26");
        assertEquals("!bitset", CLASS_ALIASES.applyAlias("java.util.BitSet").toString(), "testApplyAliasForBitSet: L27");
    }

    @Test
    public void testApplyAliasForSortedSet() {
        assertEquals("!oset", CLASS_ALIASES.applyAlias("SortedSet").toString(), "testApplyAliasForSortedSet: L32");
        assertEquals("!oset", CLASS_ALIASES.applyAlias("java.util.SortedSet").toString(), "testApplyAliasForSortedSet: L33");
    }

    @Test
    public void testApplyAliasForList() {
        assertEquals("!seq", CLASS_ALIASES.applyAlias("List").toString(), "testApplyAliasForList: L38");
        assertEquals("!seq", CLASS_ALIASES.applyAlias("java.util.List").toString(), "testApplyAliasForList: L39");
    }

    @Test
    public void testApplyAliasForMap() {
        assertEquals("!map", CLASS_ALIASES.applyAlias("Map").toString(), "testApplyAliasForMap: L44");
        assertEquals("!map", CLASS_ALIASES.applyAlias("java.util.Map").toString(), "testApplyAliasForMap: L45");
    }

    @Test
    public void testApplyAliasForSortedMap() {
        assertEquals("!omap", CLASS_ALIASES.applyAlias("SortedMap").toString(), "testApplyAliasForSortedMap: L50");
        assertEquals("!omap", CLASS_ALIASES.applyAlias("java.util.SortedMap").toString(), "testApplyAliasForSortedMap: L51");
    }

    @Test
    public void testApplyAliasForString() {
        assertEquals("String", CLASS_ALIASES.applyAlias("java.lang.String").toString(), "testApplyAliasForString: L56");
    }

    @Test
    public void testApplyAliasForByte() {
        assertEquals("byte", CLASS_ALIASES.applyAlias("Byte").toString(), "testApplyAliasForByte: L61");
        assertEquals("byte", CLASS_ALIASES.applyAlias("java.lang.Byte").toString(), "testApplyAliasForByte: L62");
    }

    @Test
    public void testApplyAliasForInteger() {
        assertEquals("int", CLASS_ALIASES.applyAlias("Integer").toString(), "testApplyAliasForInteger: L67");
        assertEquals("int", CLASS_ALIASES.applyAlias(Integer.class.getName()).toString(), "testApplyAliasForInteger: L68");
    }

    @Test
    public void testApplyAliasForLocalDate() {
        assertEquals("Date", CLASS_ALIASES.applyAlias("LocalDate").toString(), "testApplyAliasForLocalDate: L73");
        assertEquals("Date", CLASS_ALIASES.applyAlias(LocalDate.class.getName()).toString(), "testApplyAliasForLocalDate: L74");
    }

    @Test
    public void forName() {
        CLASS_ALIASES.addAlias(ClassAliasPoolTest.class);
        assertEquals("ClassAliasPoolTest", CLASS_ALIASES.applyAlias(ClassAliasPoolTest.class.getName()), "forName: L80");
        String simpleName = getClass().getSimpleName();
        assertEquals(ClassAliasPoolTest.class, CLASS_ALIASES.forName(simpleName), "forName: L82");
        StringBuilder sb = new StringBuilder(simpleName);
        assertEquals(ClassAliasPoolTest.class, CLASS_ALIASES.forName(sb), "forName: L84");
    }

    @Test
    public void addAliasViaStaticCompatibilityMethod() throws Exception {
        boolean methodInvoked = false;
        for (java.lang.reflect.Method method : ClassAliasPool.class.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isStatic(method.getModifiers())
                    && void.class.equals(method.getReturnType())
                    && method.isVarArgs()
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0] == Class[].class) {
                method.invoke(null, (Object) new Class[]{ClassAliasPoolTest.class, StringInternerTest.class});
                methodInvoked = true;
                break;
            }
        }
        if (!methodInvoked) {
            throw new AssertionError("Static compatibility method not found");
        }
        assertEquals(ClassAliasPoolTest.class, CLASS_ALIASES.forName(ClassAliasPoolTest.class.getSimpleName()), "addAliasViaStaticCompatibilityMethod: L104");
        assertEquals(StringInternerTest.class, CLASS_ALIASES.forName(StringInternerTest.class.getSimpleName()), "addAliasViaStaticCompatibilityMethod: L105");
    }

    @Test
    public void testClean() throws IllegalArgumentException {
        assertEquals("String", CLASS_ALIASES.nameFor(String.class), "testClean: L110");
        CLASS_ALIASES.clean();
        assertEquals("String", CLASS_ALIASES.nameFor(String.class), "testClean: L112");
    }

    @Test
    public void testEnum() throws IllegalArgumentException {
        assertEquals("net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum", CLASS_ALIASES.nameFor(TestEnum.class), "testEnum: L117");
        assertEquals("net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum", CLASS_ALIASES.nameFor(TestEnum.FOO.getClass()), "testEnum: L119");
        assertEquals("net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum", CLASS_ALIASES.nameFor(TestEnum.BAR.getClass()), "testEnum: L121");
    }

    @Test
    public void replace() {
        expectException("Replaced class net.openhft.chronicle.core.pool.ClassAliasPoolTest with class net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum");
        CLASS_ALIASES.addAlias(ClassAliasPoolTest.class, "name1");
        CLASS_ALIASES.addAlias(TestEnum.class, "name1");
        assertEquals(TestEnum.class, CLASS_ALIASES.forName("name1"), "replace: alias should resolve to latest type");
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

        @SuppressWarnings({"EmptyMethod", "unused"})
        void foo() {
            // No-op: placeholder method for enum constant overriding behaviour in tests
        }
    }
}
