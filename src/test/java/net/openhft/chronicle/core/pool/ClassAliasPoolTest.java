/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.util.ClassNotFoundRuntimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static net.openhft.chronicle.core.pool.ClassAliasPool.CLASS_ALIASES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("deprecation")
class ClassAliasPoolTest extends CoreTestCommon {

    @DisplayName("applyAlias resolves Set aliases correctly for types")
    @Test
    void testApplyAliasForSet() {
        assertEquals("!set", CLASS_ALIASES.applyAlias("Set").toString(), "applyAlias('Set') should resolve to '!set'");
        assertEquals("!set", CLASS_ALIASES.applyAlias("java.util.Set").toString(), "applyAlias('java.util.Set') should resolve to '!set'");
    }

    @DisplayName("applyAlias resolves BitSet aliases correctly for types")
    @Test
    void testApplyAliasForBitSet() {
        assertEquals("!bitset", CLASS_ALIASES.applyAlias("BitSet").toString(), "applyAlias('BitSet') should resolve to '!bitset'");
        assertEquals("!bitset", CLASS_ALIASES.applyAlias("java.util.BitSet").toString(), "applyAlias('java.util.BitSet') should resolve to '!bitset'");
    }

    @DisplayName("applyAlias resolves SortedSet aliases correctly for types")
    @Test
    void testApplyAliasForSortedSet() {
        assertEquals("!oset", CLASS_ALIASES.applyAlias("SortedSet").toString(), "applyAlias('SortedSet') should resolve to '!oset'");
        assertEquals("!oset", CLASS_ALIASES.applyAlias("java.util.SortedSet").toString(), "applyAlias('java.util.SortedSet') should resolve to '!oset'");
    }

    @DisplayName("applyAlias resolves List aliases correctly for types")
    @Test
    void testApplyAliasForList() {
        assertEquals("!seq", CLASS_ALIASES.applyAlias("List").toString(), "applyAlias('List') should resolve to '!seq'");
        assertEquals("!seq", CLASS_ALIASES.applyAlias("java.util.List").toString(), "applyAlias('java.util.List') should resolve to '!seq'");
    }

    @DisplayName("applyAlias resolves Map aliases correctly for types")
    @Test
    void testApplyAliasForMap() {
        assertEquals("!map", CLASS_ALIASES.applyAlias("Map").toString(), "applyAlias('Map') should resolve to '!map'");
        assertEquals("!map", CLASS_ALIASES.applyAlias("java.util.Map").toString(), "applyAlias('java.util.Map') should resolve to '!map'");
    }

    @DisplayName("applyAlias resolves SortedMap aliases correctly for types")
    @Test
    void testApplyAliasForSortedMap() {
        assertEquals("!omap", CLASS_ALIASES.applyAlias("SortedMap").toString(), "applyAlias('SortedMap') should resolve to '!omap'");
        assertEquals("!omap", CLASS_ALIASES.applyAlias("java.util.SortedMap").toString(), "applyAlias('java.util.SortedMap') should resolve to '!omap'");
    }

    @DisplayName("applyAlias keeps String name unchanged for types")
    @Test
    void testApplyAliasForString() {
        assertEquals("String", CLASS_ALIASES.applyAlias("java.lang.String").toString(), "applyAlias('java.lang.String') should resolve to 'String'");
    }

    @DisplayName("applyAlias resolves Byte aliases correctly for types")
    @Test
    void testApplyAliasForByte() {
        assertEquals("byte", CLASS_ALIASES.applyAlias("Byte").toString(), "applyAlias('Byte') should resolve to 'byte'");
        assertEquals("byte", CLASS_ALIASES.applyAlias("java.lang.Byte").toString(), "applyAlias('java.lang.Byte') should resolve to 'byte'");
    }

    @DisplayName("applyAlias resolves Integer aliases correctly for types")
    @Test
    void testApplyAliasForInteger() {
        assertEquals("int", CLASS_ALIASES.applyAlias("Integer").toString(), "applyAlias('Integer') should resolve to 'int'");
        assertEquals("int", CLASS_ALIASES.applyAlias(Integer.class.getName()).toString(), "applyAlias('java.lang.Integer') should resolve to 'int'");
    }

    @DisplayName("applyAlias resolves LocalDate aliases correctly for types")
    @Test
    void testApplyAliasForLocalDate() {
        assertEquals("Date", CLASS_ALIASES.applyAlias("LocalDate").toString(), "applyAlias('LocalDate') should resolve to 'Date'");
        assertEquals("Date", CLASS_ALIASES.applyAlias(LocalDate.class.getName()).toString(), "applyAlias('java.time.LocalDate') should resolve to 'Date'");
    }

    @DisplayName("forName resolves aliases and registered classes")
    @Test
    void forName() {
        CLASS_ALIASES.addAlias(ClassAliasPoolTest.class);
        assertEquals("ClassAliasPoolTest", CLASS_ALIASES.applyAlias(ClassAliasPoolTest.class.getName()), "applyAlias for registered class should return simple name");
        String simpleName = getClass().getSimpleName();
        assertEquals(ClassAliasPoolTest.class, CLASS_ALIASES.forName(simpleName), "forName with simple name should resolve to registered class");
        StringBuilder sb = new StringBuilder(simpleName);
        assertEquals(ClassAliasPoolTest.class, CLASS_ALIASES.forName(sb), "forName with StringBuilder should resolve to registered class");
    }

    @DisplayName("static addAlias method registers aliases behaviour under expected input and output conditions")
    @Test
    void addAliasViaStaticCompatibilityMethod() throws Exception {
        boolean methodInvoked = false;
        for (java.lang.reflect.Method method : ClassAliasPool.class.getDeclaredMethods()) {
                if (java.lang.reflect.Modifier.isStatic(method.getModifiers())
                    && void.class.equals(method.getReturnType())
                    && method.isVarArgs()
                    && method.getParameterCount() == 1
                    && "java.lang.Class[]".equals(method.getParameterTypes()[0].getTypeName())) {
                Class<?>[] aliases = {ClassAliasPoolTest.class, StringInternerTest.class};
                method.invoke(null, (Object) aliases);
                methodInvoked = true;
                break;
            }
        }
        if (!methodInvoked) {
            throw new AssertionError("Static compatibility method not found");
        }
        assertEquals(ClassAliasPoolTest.class, CLASS_ALIASES.forName(ClassAliasPoolTest.class.getSimpleName()), "forName should resolve ClassAliasPoolTest after static alias registration");
        assertEquals(StringInternerTest.class, CLASS_ALIASES.forName(StringInternerTest.class.getSimpleName()), "forName should resolve StringInternerTest after static alias registration");
    }

    @DisplayName("clean preserves core aliases after reset")
    @Test
    void testClean() throws IllegalArgumentException {
        assertEquals("String", CLASS_ALIASES.nameFor(String.class), "nameFor(String.class) should return 'String' before clean");
        CLASS_ALIASES.clean();
        assertEquals("String", CLASS_ALIASES.nameFor(String.class), "nameFor(String.class) should return 'String' after clean");
    }

    @DisplayName("nameFor returns enum type name behaviour under expected input and output conditions")
    @Test
    void testEnum() throws IllegalArgumentException {
        assertEquals("net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum", CLASS_ALIASES.nameFor(TestEnum.class), "nameFor(TestEnum.class) should return full qualified name");
        assertEquals("net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum", CLASS_ALIASES.nameFor(TestEnum.FOO.getClass()), "nameFor(TestEnum.FOO) should return enum type name not anonymous subclass");
        assertEquals("net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum", CLASS_ALIASES.nameFor(TestEnum.BAR.getClass()), "nameFor(TestEnum.BAR) should return enum type name");
    }

    @DisplayName("addAlias replaces existing alias mapping behaviour under expected input and output conditions")
    @Test
    void replace() {
        expectException("Replaced class net.openhft.chronicle.core.pool.ClassAliasPoolTest with class net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum");
        CLASS_ALIASES.addAlias(ClassAliasPoolTest.class, "name1");
        CLASS_ALIASES.addAlias(TestEnum.class, "name1");
        assertEquals(TestEnum.class, CLASS_ALIASES.forName("name1"), "replace: alias should resolve to latest type");
    }

    /**
     * On Windows this would cause a NoClassDefFoundError
     */
    @DisplayName("forName rejects wrong case class name")
    @Test
    void wrongCaseClassName() {
        assertThrows(ClassNotFoundRuntimeException.class,
                () -> CLASS_ALIASES.forName(TestEnum.class.getName().toLowerCase()),
                "forName should reject lower-case class name aliases");
    }

    @DisplayName("forName rejects banned internal classes behaviour under expected input and output conditions")
    @Test
    void banned() {
        for (int i = 0; i < 2; i++) {
            assertThrows(ClassNotFoundRuntimeException.class,
                    () -> CLASS_ALIASES.forName("com.sun.xml.internal.bind.v2.runtime.unmarshaller.Base64Data"),
                    "forName should reject banned class Base64Data i=" + i);
            assertThrows(ClassNotFoundRuntimeException.class,
                    () -> CLASS_ALIASES.forName("com.sun.istack.internal.ByteArrayDataSource"),
                    "forName should reject banned class ByteArrayDataSource i=" + i);
            assertThrows(ClassNotFoundRuntimeException.class,
                    () -> CLASS_ALIASES.forName("com.oracle.webservices.internal.api.databinding.DatabindingFactory"),
                    "forName should reject banned class DatabindingFactory i=" + i);
            assertThrows(ClassNotFoundRuntimeException.class,
                    () -> CLASS_ALIASES.forName("jdk.internal.util.xml.SAXParser"),
                    "forName should reject banned class SAXParser i=" + i);
            assertThrows(ClassNotFoundRuntimeException.class,
                    () -> CLASS_ALIASES.forName("sun.corba.SharedSecrets"),
                    "forName should reject banned class SharedSecrets i=" + i);
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
