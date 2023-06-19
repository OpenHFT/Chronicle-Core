package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.io.ReferenceOwner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReferenceOwnerGptTest {
    private static class TestReferenceOwner implements ReferenceOwner {
        // This class doesn't need to add any behavior, it just needs to exist so we can create instances
    }

    @Test
    public void testReferenceId() {
        TestReferenceOwner testReferenceOwner = new TestReferenceOwner();
        int expectedId = System.identityHashCode(testReferenceOwner);
        
        assertEquals(expectedId, testReferenceOwner.referenceId());
    }

    @Test
    public void testReferenceName() {
        TestReferenceOwner testReferenceOwner = new TestReferenceOwner();
        String expectedName = "TestReferenceOwner@" + Integer.toString(testReferenceOwner.referenceId(), 36);
        
        assertEquals(expectedName, testReferenceOwner.referenceName());
    }

    @Test
    public void testTemporary() {
        // You'll need to ensure that Jvm.isResourceTracing() returns false for this test
        ReferenceOwner tmpOwner = ReferenceOwner.temporary("temp");
        
        // Check that the correct instance is returned when resource tracing is off
        assertEquals(ReferenceOwner.TMP, tmpOwner);

        // TODO: You should add a similar test for when Jvm.isResourceTracing() returns true, but you'll need
        // to find a way to toggle it on for the test. Keep in mind that it should be toggled back off in a
        // @AfterEach or @AfterAll method to ensure other tests aren't affected.
    }
}
