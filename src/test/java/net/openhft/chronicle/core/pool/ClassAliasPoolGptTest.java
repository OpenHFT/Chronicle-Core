package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.util.ClassNotFoundRuntimeException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ClassAliasPoolGptTest {

    private ClassAliasPool classAliasPool;

    @Before
    public void setUp() {
        classAliasPool = new ClassAliasPool(ClassAliasPool.CLASS_ALIASES);
    }

    @Test
    public void testForName() {
        // Check default aliases
        assertEquals(String.class, classAliasPool.forName("String"));
        assertEquals(Long.class, classAliasPool.forName("Long"));

        // Test for non-existent class
        try {
            classAliasPool.forName("NonExistentClass");
            fail("Exception should have been thrown");
        } catch (ClassNotFoundRuntimeException e) {
            // Expected
        }
    }

    @Test
    public void testNameFor() {
        // Check default aliases
        assertEquals("String", classAliasPool.nameFor(String.class));
        assertEquals("long", classAliasPool.nameFor(Long.class));

        // Test for anonymous class
        Object o = new Object() {
        };
        assertEquals(o.getClass().getName(), classAliasPool.nameFor(o.getClass()));
    }

    @Test
    public void testAddAlias() {
        classAliasPool.addAlias(HashMap.class, "HashMap, MyMap");

        try {
            assertEquals(HashMap.class, classAliasPool.forName("HashMap"));
            assertEquals(HashMap.class, classAliasPool.forName("MyMap"));
        } catch (ClassNotFoundRuntimeException e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    public void testRemovePackage() {
        // Add custom class with package
        classAliasPool.addAlias(HashMap.class, "HashMap, MyMap");
        assertEquals("HashMap", classAliasPool.nameFor(HashMap.class));

        classAliasPool.addAlias(HashSet.class, "MySet, HashSet");
        assertEquals("MySet", classAliasPool.nameFor(HashSet.class));

        // Remove package
        classAliasPool.removePackage("java.util");
        assertEquals(HashMap.class.getName(), classAliasPool.nameFor(HashMap.class));
    }
}
