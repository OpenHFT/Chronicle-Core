/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This class has no package declaration to exercise Jvm package name lookup logic.
 */
@SuppressWarnings("PMD.NoPackage")
class ClassWithNoPackageTest {
    @Test
    void getPackageName() {
        assertEquals("", Jvm.getPackageName(ClassWithNoPackageTest.class), "package name should be empty string for class with no package");
    }
}
