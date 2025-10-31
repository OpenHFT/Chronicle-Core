/*
 * Copyright 2016-2025 chronicle.software
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
