package net.openhft.chronicle.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClassLocalTest {

    @Test
    public void computeValue() {
        long[] count = {0};
        ClassLocal<String> toString = ClassLocal.withInitial(aClass -> {
            System.out.println(aClass);
            count[0]++;
            return aClass.toGenericString();
        });
        for (int i = 0; i < 1000; i++) {
            toString.get(ClassValue.class);
        }
        assertEquals(1, count[0]);

    }
}