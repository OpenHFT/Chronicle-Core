/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Maths;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnumInternerTest extends CoreTestCommon {

    private static final int MASK = 63;

    public static void main(String[] args) {
        for (int i = 0; i < 100000; i++) {
            String s = Long.toString(i, 36);
            if (!Character.isJavaIdentifierStart(s.charAt(0)))
                continue;
            long h1 = Maths.hash64(s);
            h1 ^= h1 >> 32;
            int h = (int) h1 & MASK;
            if (h == 0)
                System.out.println(s + ",");
        }
    }

    @DisplayName("Enum interner handles hash collisions in parallel")
    @Test
    void clashTest() {
        for (TestEnum value : TestEnum.values()) {
            @NotNull String s = value.toString();
            long h = Maths.hash64(s);
            h ^= h >> 32;
            assertEquals(0, (int) h & MASK, "Hash value should produce zero when masked with MASK for " + s);
        }

        final EnumInterner<TestEnum> testEnum
                = new EnumInterner<>(TestEnum.class);

        Stream.of(TestEnum.values())
                .parallel()
                .forEach(te -> {
                    final String cs = te.toString();
                    for (int i = 0; i < 20000; i++) {
                        final TestEnum interned = testEnum.intern(cs);
                        assertEquals(te, interned, "Interned enum should match original enum value for parallel access iteration " + i);
                    }
                });
    }

    @SuppressWarnings("java:S115")
    enum TestEnum {
        c1, cq, db, ho, id, k6, kv, la, m5, mu, nb, qg, s8, sx, uz, va, yj
    }
}
