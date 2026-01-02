/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.util.ClassNotFoundRuntimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static net.openhft.chronicle.core.pool.ClassAliasPool.CLASS_ALIASES;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
class ClassAliasPoolTest extends CoreTestCommon {

    @Test
    @DisplayName("applyAlias resolves Set aliases correctly for types")
    void testApplyAliasForSet() {
        assertEquals("!set", CLASS_ALIASES.applyAlias("Set").toString(), "applyAlias('Set') should resolve to '!set'");
        assertEquals("!set", CLASS_ALIASES.applyAlias("java.util.Set").toString(), "applyAlias('java.util.Set') should resolve to '!set'");
    }

    @Test
    @DisplayName("applyAlias resolves BitSet aliases correctly for types")
    void testApplyAliasForBitSet() {
        assertEquals("!bitset", CLASS_ALIASES.applyAlias("BitSet").toString(), "applyAlias('BitSet') should resolve to '!bitset'");
        assertEquals("!bitset", CLASS_ALIASES.applyAlias("java.util.BitSet").toString(), "applyAlias('java.util.BitSet') should resolve to '!bitset'");
    }

    @Test
    @DisplayName("applyAlias resolves SortedSet aliases correctly for types")
    void testApplyAliasForSortedSet() {
        assertEquals("!oset", CLASS_ALIASES.applyAlias("SortedSet").toString(), "applyAlias('SortedSet') should resolve to '!oset'");
        assertEquals("!oset", CLASS_ALIASES.applyAlias("java.util.SortedSet").toString(), "applyAlias('java.util.SortedSet') should resolve to '!oset'");
    }

    @Test
    @DisplayName("applyAlias resolves List aliases correctly for types")
    void testApplyAliasForList() {
        assertEquals("!seq", CLASS_ALIASES.applyAlias("List").toString(), "applyAlias('List') should resolve to '!seq'");
        assertEquals("!seq", CLASS_ALIASES.applyAlias("java.util.List").toString(), "applyAlias('java.util.List') should resolve to '!seq'");
    }

    @Test
    @DisplayName("applyAlias resolves Map aliases correctly for types")
    void testApplyAliasForMap() {
        assertEquals("!map", CLASS_ALIASES.applyAlias("Map").toString(), "applyAlias('Map') should resolve to '!map'");
        assertEquals("!map", CLASS_ALIASES.applyAlias("java.util.Map").toString(), "applyAlias('java.util.Map') should resolve to '!map'");
    }

    @Test
    @DisplayName("applyAlias resolves SortedMap aliases correctly for types")
    void testApplyAliasForSortedMap() {
        assertEquals("!omap", CLASS_ALIASES.applyAlias("SortedMap").toString(), "applyAlias('SortedMap') should resolve to '!omap'");
        assertEquals("!omap", CLASS_ALIASES.applyAlias("java.util.SortedMap").toString(), "applyAlias('java.util.SortedMap') should resolve to '!omap'");
    }

    @Test
    @DisplayName("applyAlias keeps String name unchanged for types")
    void testApplyAliasForString() {
        assertEquals("String", CLASS_ALIASES.applyAlias("java.lang.String").toString(), "applyAlias('java.lang.String') should resolve to 'String'");
    }

    @Test
    @DisplayName("applyAlias resolves Byte aliases correctly for types")
    void testApplyAliasForByte() {
        assertEquals("byte", CLASS_ALIASES.applyAlias("Byte").toString(), "applyAlias('Byte') should resolve to 'byte'");
        assertEquals("byte", CLASS_ALIASES.applyAlias("java.lang.Byte").toString(), "applyAlias('java.lang.Byte') should resolve to 'byte'");
    }

    @Test
    @DisplayName("applyAlias resolves Integer aliases correctly for types")
    void testApplyAliasForInteger() {
        assertEquals("int", CLASS_ALIASES.applyAlias("Integer").toString(), "applyAlias('Integer') should resolve to 'int'");
        assertEquals("int", CLASS_ALIASES.applyAlias(Integer.class.getName()).toString(), "applyAlias('java.lang.Integer') should resolve to 'int'");
    }

    @Test
    @DisplayName("applyAlias resolves LocalDate aliases correctly for types")
    void testApplyAliasForLocalDate() {
        assertEquals("Date", CLASS_ALIASES.applyAlias("LocalDate").toString(), "applyAlias('LocalDate') should resolve to 'Date'");
        assertEquals("Date", CLASS_ALIASES.applyAlias(LocalDate.class.getName()).toString(), "applyAlias('java.time.LocalDate') should resolve to 'Date'");
    }

    @Test
    @DisplayName("forName resolves aliases and registered classes")
    void forName() {
        CLASS_ALIASES.addAlias(ClassAliasPoolTest.class);
        assertEquals("ClassAliasPoolTest", CLASS_ALIASES.applyAlias(ClassAliasPoolTest.class.getName()), "applyAlias for registered class should return simple name");
        String simpleName = getClass().getSimpleName();
        assertEquals(ClassAliasPoolTest.class, CLASS_ALIASES.forName(simpleName), "forName with simple name should resolve to registered class");
        StringBuilder sb = new StringBuilder(simpleName);
        assertEquals(ClassAliasPoolTest.class, CLASS_ALIASES.forName(sb), "forName with StringBuilder should resolve to registered class");
    }

    @Test
    @DisplayName("Static add alias method registers aliases")
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

    @Test
    @DisplayName("clean preserves core aliases after reset")
    void testClean() throws IllegalArgumentException {
        assertEquals("String", CLASS_ALIASES.nameFor(String.class), "nameFor(String.class) should return 'String' before clean");
        CLASS_ALIASES.clean();
        assertEquals("String", CLASS_ALIASES.nameFor(String.class), "nameFor(String.class) should return 'String' after clean");
    }

    @Test
    @DisplayName("Name for returns enum type name alias")
    void testEnum() throws IllegalArgumentException {
        assertEquals("net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum", CLASS_ALIASES.nameFor(TestEnum.class), "nameFor(TestEnum.class) should return full qualified name");
        assertEquals("net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum", CLASS_ALIASES.nameFor(TestEnum.FOO.getClass()), "nameFor(TestEnum.FOO) should return enum type name not anonymous subclass");
        assertEquals("net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum", CLASS_ALIASES.nameFor(TestEnum.BAR.getClass()), "nameFor(TestEnum.BAR) should return enum type name");
    }

    @Test
    @DisplayName("Add alias replaces existing alias mapping")
    void replace() {
        expectException("Replaced class net.openhft.chronicle.core.pool.ClassAliasPoolTest with class net.openhft.chronicle.core.pool.ClassAliasPoolTest$TestEnum");
        CLASS_ALIASES.addAlias(ClassAliasPoolTest.class, "name1");
        CLASS_ALIASES.addAlias(TestEnum.class, "name1");
        assertEquals(TestEnum.class, CLASS_ALIASES.forName("name1"), "replace: alias should resolve to latest type");
    }

    /**
     * On Windows this would cause a NoClassDefFoundError
     */
    @Test
    @DisplayName("forName rejects wrong case class name")
    void wrongCaseClassName() {
        Class<? extends Throwable> expected = (OS.isWindows() || OS.isWsl())
                ? NoClassDefFoundError.class
                : ClassNotFoundRuntimeException.class;
        String expectedName = expected.getSimpleName();
        assertThrows(expected,
                () -> CLASS_ALIASES.forName(TestEnum.class.getName().toLowerCase()),
                "forName should reject lower-case class name aliases with " + expectedName);
    }

    @Test
    @DisplayName("For name rejects banned internal classes")
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

    // --- Additional tests for branch coverage ---

    @Test
    @DisplayName("forName throws for empty class name")
    void forNameEmptyName() {
        assertThrows(ClassNotFoundRuntimeException.class,
                () -> CLASS_ALIASES.forName(""),
                "forName should throw for empty name");
    }

    @Test
    @DisplayName("forName caches resolution failures")
    void forNameCachesFailure() {
        String invalidName = "nonexistent.class.Name" + System.nanoTime();
        // First call - should throw
        assertThrows(ClassNotFoundRuntimeException.class,
                () -> CLASS_ALIASES.forName(invalidName),
                "first forName call should throw");
        // Second call - should throw the cached exception
        assertThrows(ClassNotFoundRuntimeException.class,
                () -> CLASS_ALIASES.forName(invalidName),
                "second forName call should throw cached exception");
    }

    @Test
    @DisplayName("resetResolutionFailures clears exception cache")
    void resetResolutionFailuresTest() {
        String invalidName = "nonexistent.class.ResetTest" + System.nanoTime();
        // Trigger caching
        assertThrows(ClassNotFoundRuntimeException.class,
                () -> CLASS_ALIASES.forName(invalidName),
                "forName should throw for invalid name");
        // Reset cache
        CLASS_ALIASES.resetResolutionFailures();
        // Should still throw (because class doesn't exist) but via fresh lookup
        assertThrows(ClassNotFoundRuntimeException.class,
                () -> CLASS_ALIASES.forName(invalidName),
                "forName should throw after cache reset");
    }

    @Test
    @DisplayName("forName with StringBuilder uses CharSequence hashCode path")
    void forNameWithStringBuilder() {
        CLASS_ALIASES.addAlias(ClassAliasPoolTest.class);
        StringBuilder sb = new StringBuilder("ClassAliasPoolTest");
        assertEquals(ClassAliasPoolTest.class, CLASS_ALIASES.forName(sb),
                "forName with StringBuilder should resolve via CharSequence path");
    }

    @Test
    @DisplayName("CAPKey equals returns false for non-CharSequence")
    void capKeyEqualsNonCharSequence() {
        ClassAliasPool.CAPKey key = new ClassAliasPool.CAPKey("test");
        assertNotEquals(key, Integer.valueOf(123), "CAPKey should not equal Integer");
        assertNotEquals(key, new Object(), "CAPKey should not equal Object");
    }

    @Test
    @DisplayName("CAPKey equals compares length before content")
    void capKeyEqualsLengthMismatch() {
        ClassAliasPool.CAPKey key1 = new ClassAliasPool.CAPKey("test");
        ClassAliasPool.CAPKey key2 = new ClassAliasPool.CAPKey("testing");
        assertNotEquals(key1, key2, "CAPKeys with different lengths should not be equal");
    }

    @Test
    @DisplayName("CAPKey equals compares character by character for non-String")
    void capKeyEqualsCharByChar() {
        ClassAliasPool.CAPKey key1 = new ClassAliasPool.CAPKey("test");
        StringBuilder sb = new StringBuilder("test");
        ClassAliasPool.CAPKey key2 = new ClassAliasPool.CAPKey(null);
        key2.value = sb;
        assertEquals(key1, key2, "CAPKey with String should equal CAPKey with same StringBuilder content");
    }

    @Test
    @DisplayName("CAPKey equals returns false for different characters")
    void capKeyEqualsDifferentChars() {
        ClassAliasPool.CAPKey key1 = new ClassAliasPool.CAPKey("test");
        StringBuilder sb = new StringBuilder("teST");
        ClassAliasPool.CAPKey key2 = new ClassAliasPool.CAPKey(null);
        key2.value = sb;
        assertNotEquals(key1, key2, "CAPKeys with different characters should not be equal");
    }

    @Test
    @DisplayName("CAPKey hashCode handles non-String CharSequence")
    void capKeyHashCodeNonString() {
        StringBuilder sb = new StringBuilder("test");
        ClassAliasPool.CAPKey key = new ClassAliasPool.CAPKey(null);
        key.value = sb;
        // hashCode should compute without throwing
        int hash = key.hashCode();
        // Should match String hashCode algorithm
        assertEquals("test".hashCode(), hash, "hashCode for StringBuilder should match String hashCode");
    }

    @Test
    @DisplayName("CAPKey subSequence throws UnsupportedOperationException")
    void capKeySubSequence() {
        ClassAliasPool.CAPKey key = new ClassAliasPool.CAPKey("test");
        assertThrows(UnsupportedOperationException.class,
                () -> key.subSequence(0, 2),
                "subSequence should throw UnsupportedOperationException");
    }

    @Test
    @DisplayName("nameFor with lambda throws IllegalArgumentException")
    void nameForLambda() {
        Runnable lambda = () -> { };
        assertThrows(IllegalArgumentException.class,
                () -> CLASS_ALIASES.nameFor(lambda.getClass()),
                "nameFor should throw for lambda class");
    }

    @Test
    @DisplayName("nameFor returns full name for unregistered class")
    void nameForUnregisteredClass() {
        // Use a fresh pool without registrations
        ClassAliasPool pool = new ClassAliasPool(null);
        String name = pool.nameFor(UnregisteredTestClass.class);
        // Should return full name since class is not registered
        assertEquals(UnregisteredTestClass.class.getName(), name,
                "nameFor should return full name for unregistered class");
    }

    // Helper class that is not registered
    static class UnregisteredTestClass {
    }

    @Test
    @DisplayName("removePackage removes matching aliases")
    void removePackageTest() {
        ClassAliasPool pool = new ClassAliasPool(null);
        pool.addAlias(ClassAliasPoolTest.class);
        assertEquals(ClassAliasPoolTest.class, pool.forName("ClassAliasPoolTest"),
                "class should be registered");
        pool.removePackage("net.openhft.chronicle.core.pool");
        assertThrows(ClassNotFoundRuntimeException.class,
                () -> pool.forName("ClassAliasPoolTest"),
                "class should be removed after removePackage");
    }

    @Test
    @DisplayName("testPackage returns true for matching package")
    void testPackageMatches() {
        assertTrue(ClassAliasPool.testPackage("java.lang", String.class),
                "String should be in java.lang package");
        assertTrue(ClassAliasPool.testPackage("net.openhft", ClassAliasPoolTest.class),
                "ClassAliasPoolTest should be in net.openhft package");
    }

    @Test
    @DisplayName("testPackage returns false for non-matching package")
    void testPackageNoMatch() {
        assertFalse(ClassAliasPool.testPackage("com.example", String.class),
                "String should not be in com.example package");
    }

    @Test
    @DisplayName("ClassAliasPool constructor with parent derives classLoader")
    void constructorWithParent() {
        ClassAliasPool parent = new ClassAliasPool(null);
        ClassAliasPool child = new ClassAliasPool(parent);
        // Child should be able to resolve classes
        assertEquals(String.class, child.forName("java.lang.String"),
                "child pool should resolve String");
    }

    @Test
    @DisplayName("addAlias with multiple names registers all aliases")
    void addAliasMultipleNames() {
        ClassAliasPool pool = new ClassAliasPool(null);
        pool.addAlias(String.class, "Str, Text, Txt");
        assertEquals(String.class, pool.forName("Str"), "Str alias should resolve to String");
        assertEquals(String.class, pool.forName("Text"), "Text alias should resolve to String");
        assertEquals(String.class, pool.forName("Txt"), "Txt alias should resolve to String");
    }
}
