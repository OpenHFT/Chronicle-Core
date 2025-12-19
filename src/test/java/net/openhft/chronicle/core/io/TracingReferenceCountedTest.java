/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static net.openhft.chronicle.core.internal.CloseableUtils.asString;
import static org.junit.jupiter.api.Assertions.*;

public class TracingReferenceCountedTest extends MonitorReferenceCountedContractTest {

    private AtomicInteger onReleaseCallCount;

    @BeforeEach
    public void setUp() {
        onReleaseCallCount = new AtomicInteger(0);
    }

    @Override
    protected TracingReferenceCounted createReferenceCounted() {
        return new TracingReferenceCounted(onReleaseCallCount::incrementAndGet, "uniqueId", TracingReferenceCounted.class);
    }

    @Test
    public void reserveWillThrowAndNotReserveWhenReferenceOwnerAttemptsToMakeASecondReservation() {
        TracingReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        referenceCounted.reserve(a);

        assertEquals(2, referenceCounted.refCount(), "refCount should be 2 after initial reserve");
        assertThrows(IllegalStateException.class, () -> referenceCounted.reserve(a));
        assertEquals(2, referenceCounted.refCount(), "refCount should remain 2 after failed duplicate reserve");
    }

    @Test
    public void releaseWillFailWhenResourceOwnerHasNoReservation() {
        TracingReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        assertThrows(IllegalStateException.class, () -> referenceCounted.release(a));
    }

    @Test
    public void releaseLastWillThrowWithReferenceDetailsWhenReleaseIsNotLast() {
        TracingReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        referenceCounted.reserve(a);
        try {
            referenceCounted.releaseLast(a);
            fail("Release last should throw here");
        } catch (IllegalStateException e) {
            assertEquals("net.openhft.chronicle.core.io.TracingReferenceCounted still reserved [INIT]", e.getMessage(), "exception message should indicate resource still reserved");
            assertEquals("uniqueId main init INIT on main", e.getSuppressed()[0].getMessage().split(" at ")[0], "suppressed exception should contain INIT reservation details");
        }
    }

    @Test
    public void releaseLastWillThrowWithSuppressedInnerFailuresWhenReleaseFails() {
        TracingReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        try {
            referenceCounted.releaseLast(a);
            fail("Release last should throw here");
        } catch (IllegalStateException e) {
            assertEquals("net.openhft.chronicle.core.io.TracingReferenceCounted still reserved [INIT]", e.getMessage(), "exception message should indicate resource still reserved");
            assertEquals("uniqueId main init INIT on main", e.getSuppressed()[0].getMessage().split(" at ")[0], "first suppressed exception should contain INIT reservation details");
            assertEquals("net.openhft.chronicle.core.io.TracingReferenceCounted not reserved by VanillaReferenceOwner{name='a'} closed=false", e.getSuppressed()[1].getMessage(), "second suppressed exception should indicate owner 'a' has no reservation");
        }
    }

    @Test
    public void asStringWillIncludeReferenceCountedDetails() {
        final TracingReferenceCounted referenceCounted = createReferenceCounted();
        assertTrue(Pattern.matches("TracingReferenceCounted@\\w+ refCount=1", asString(referenceCounted)), "asString output should include class name and refCount");
    }

    @Test
    public void asStringWillIncludeCloseableDetails() {
        class SomeCloseable implements QueryCloseable, ReferenceOwner {

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public String referenceName() {
                return "testCloseable";
            }
        }
        assertEquals("testCloseable closed=false", asString(new SomeCloseable()), "asString output should include reference name and closed state");
    }

    @Test
    public void asStringRenderClassNameAndAddressForPojos() {
        class SomePlainObject {

        }
        assertTrue(Pattern.matches("SomePlainObject@\\w+", asString(new SomePlainObject())), "asString output should include class name and memory address for plain objects");
    }

    @Test
    public void createdHereWillReturnCreatedStackTrace() {
        TracingReferenceCounted referenceCounted = createReferenceCounted();

        assertNotNull(referenceCounted.createdHere(), "createdHere should return non-null stack trace");
    }

    @Test
    public void reserveTransferWillThrowWhenFromHasNoReservation() {
        TracingReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        ReferenceOwner b = ReferenceOwner.temporary("b");

        assertThrows(IllegalStateException.class, () -> referenceCounted.reserveTransfer(a, b));
    }

    @Test
    public void reserveTransferWillThrowWhenToAlreadyHasAReservation() {
        TracingReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        ReferenceOwner b = ReferenceOwner.temporary("b");
        referenceCounted.reserve(a);
        referenceCounted.reserve(b);

        assertThrows(IllegalStateException.class, () -> referenceCounted.reserveTransfer(a, b));
    }

    @Test
    public void reserveWillThrowWhenCalledWithSelf() {
        TracingReferenceCounted referenceCounted = createReferenceCounted();

        assertThrows(AssertionError.class, () -> referenceCounted.reserve(referenceCounted));
    }
}
