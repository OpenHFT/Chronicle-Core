package net.openhft.chronicle.core.io;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class ReferenceOwnerTest extends TestCase {
    @Test
    public void testReferenceId() {
        Set<Integer> ints = new HashSet<>();
        for (int i = 0; i < 101; i++)
            ints.add(new VanillaReferenceOwner("hi").referenceId());
        assertEquals(100, ints.size(), 1);
    }
}