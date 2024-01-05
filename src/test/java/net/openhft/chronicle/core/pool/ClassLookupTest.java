package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.util.ClassNotFoundRuntimeException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class ClassLookupTest {

    private ClassLookup classLookup = ClassAliasPool.CLASS_ALIASES;

    @Test
    void testClassLookupByName() {
        Class<?> clazz = classLookup.forName("java.lang.String");
        assertEquals(String.class, clazz);
    }

    @Test
    void testAddingAliasAndLookupByAlias() {
        classLookup.addAlias(String.class, "StringAlias");
        Class<?> clazz = classLookup.forName("StringAlias");
        assertEquals(String.class, clazz);
    }

    @Test
    void testImmutabilityOfWrappedInstance() {
        ClassLookup wrapped = classLookup.wrap();
        wrapped.addAlias(String.class, "StringAlias");

        assertThrows(ClassNotFoundRuntimeException.class, () -> classLookup.forName("StringAlias"));
    }

    @Test
    void testLookupOfLambdaClass() {
        Runnable lambda = () -> {};
        assertThrows(IllegalArgumentException.class, () -> classLookup.nameFor(lambda.getClass()));
    }

    // Additional tests as necessary...
}
