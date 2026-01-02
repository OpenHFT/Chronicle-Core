/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class without a package declaration to exercise package name lookup behaviour in Jvm edge cases.
 */
@SuppressWarnings("PMD.NoPackage")
class ClassWithNoPackageTest {
    @Test
    @DisplayName("package name resolves to empty string without package")
    void getPackageName() {
        assertEquals("", Jvm.getPackageName(ClassWithNoPackageTest.class), "package name should be empty string for class with no package");
    }
}
