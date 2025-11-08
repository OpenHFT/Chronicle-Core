/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.system;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class SystemPropertiesPrecedenceTest {

    private static final String TEST_KEY = "chronicle.core.test.prop";
    private static final Method LOAD_METHOD;

    static {
        try {
            LOAD_METHOD = Jvm.class.getDeclaredMethod("loadSystemProperties", String.class, boolean.class);
            LOAD_METHOD.setAccessible(true);
        } catch (Exception e) {
            throw new AssertionError("Unable to access Jvm#loadSystemProperties", e);
        }
    }

    @AfterEach
    void cleanUp() {
        System.clearProperty(TEST_KEY);
        System.clearProperty("system.properties");
    }

    @Test
    void fileValuesPopulateUnsetProperties() throws Exception {
        Path tempFile = Files.createTempFile("system-properties-precedence", ".properties");
        Files.write(tempFile, (TEST_KEY + "=file-value\n").getBytes());

        invokeLoader(tempFile.toString(), true);

        assertEquals("file-value", System.getProperty(TEST_KEY));
    }

    @Test
    void commandLineOverridesWinOverFile() throws Exception {
        Path tempFile = Files.createTempFile("system-properties-precedence", ".properties");
        Files.write(tempFile, (TEST_KEY + "=file-value\n").getBytes());
        System.setProperty(TEST_KEY, "cmd-value");

        invokeLoader(tempFile.toString(), true);

        assertEquals("cmd-value", System.getProperty(TEST_KEY), "putIfAbsent must preserve existing values");
    }

    @Test
    void classpathResourceIsLoadedWhenNoSystemPropertySet() throws Exception {
        System.clearProperty(TEST_KEY);

        invokeLoader("custom-system.properties", false);

        assertEquals("resource-value", System.getProperty(TEST_KEY));
    }

    private static void invokeLoader(String name, boolean wasSet) throws Exception {
        LOAD_METHOD.invoke(null, name, wasSet);
    }
}
