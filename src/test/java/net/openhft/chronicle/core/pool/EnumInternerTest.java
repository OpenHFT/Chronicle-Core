package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.Maths;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnumInternerTest {

    static final int MASK = 63;

    public static void main(String[] args) {
        for (int i = 0; i < 100000; i++) {
            String s = Long.toString(i, 36);
            if (!Character.isJavaIdentifierStart(s.charAt(0)))
                continue;
            int h = Maths.hash32(s) & MASK;
            if (h == 0)
                System.out.println(s + ",");
        }
    }

    @Test
    public void clashTest() {
        for (TestEnum value : TestEnum.values()) {
            assertEquals(0, Maths.hash32(value.toString()) & MASK);
        }

        final EnumInterner<TestEnum> testEnum
                = new EnumInterner<>(TestEnum.class);

        Stream.of(TestEnum.values())
                .parallel()
                .forEach(te -> {
                    final String cs = te.toString();
                    for (int i = 0; i < 20000; i++) {
                        final TestEnum interned = testEnum.intern(cs);
                        Assert.assertEquals("i: " + i, interned, te);
                    }
                });
    }

    enum TestEnum {
        c1, cq, db, ho, id, k6, kv, la, m5, mu, nb, qg, s8, sx, uz, va, yj
    }
}