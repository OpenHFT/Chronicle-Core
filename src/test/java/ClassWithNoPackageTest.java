/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This class has no package declaration.
 */
public class ClassWithNoPackageTest {
    @Test
    public void getPackageName() {
        assertEquals("", Jvm.getPackageName(ClassWithNoPackageTest.class), "getPackageName: L15");
    }
}
