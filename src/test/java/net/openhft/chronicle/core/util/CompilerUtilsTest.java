package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CompilerUtilsTest {
    @Test
    void defineClassShouldThrowAssertionErrorForIllegalAccessException() {
        ClassLoader classLoader = mock(ClassLoader.class);
        String className = "com.example.MyClass";
        byte[] bytes = new byte[] { /* class file bytes */ };

        // Simulate IllegalAccessException
        assertThrows(AssertionError.class, () -> CompilerUtils.defineClass(classLoader, className, bytes));
    }

    @Test
    void defineClassShouldThrowAssertionErrorForInvocationTargetException() {
        ClassLoader classLoader = mock(ClassLoader.class);
        String className = "com.example.MyClass";
        byte[] bytes = new byte[] { /* class file bytes */ };

        // Simulate InvocationTargetException
        assertThrows(AssertionError.class, () -> CompilerUtils.defineClass(classLoader, className, bytes));
    }
}
