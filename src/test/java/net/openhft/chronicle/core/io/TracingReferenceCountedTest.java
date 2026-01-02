/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static net.openhft.chronicle.core.internal.CloseableUtils.asString;
import static org.junit.jupiter.api.Assertions.*;

class TracingReferenceCountedTest extends MonitorReferenceCountedContractTest {

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
    @DisplayName("reserve rejects duplicate reservation from same owner")
    void reserveWillThrowAndNotReserveWhenReferenceOwnerAttemptsToMakeASecondReservation() {
        TracingReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        referenceCounted.reserve(a);

        assertEquals(2, referenceCounted.refCount(), "refCount should be 2 after initial reserve");
        assertThrows(IllegalStateException.class, () -> referenceCounted.reserve(a),
                "reserve should fail on duplicate owner reservation");
        assertEquals(2, referenceCounted.refCount(), "refCount should remain 2 after failed duplicate reserve");
    }

    @Test
    @DisplayName("release fails when owner has no reservation")
    void releaseWillFailWhenResourceOwnerHasNoReservation() {
        TracingReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        assertThrows(IllegalStateException.class, () -> referenceCounted.release(a),
                "release should fail when owner has no reservation");
    }

    @Test
    @DisplayName("releaseLast throws with details when not last")
    void releaseLastWillThrowWithReferenceDetailsWhenReleaseIsNotLast() {
        TracingReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        referenceCounted.reserve(a);
        try {
            referenceCounted.releaseLast(a);
            fail("releaseLast should throw when INIT remains reserved");
        } catch (IllegalStateException e) {
            assertEquals("net.openhft.chronicle.core.io.TracingReferenceCounted still reserved [INIT]", e.getMessage(), "exception message should indicate resource still reserved");
            assertEquals("uniqueId main init INIT on main", e.getSuppressed()[0].getMessage().split(" at ")[0], "suppressed exception should contain INIT reservation details");
        }
    }

    @Test
    @DisplayName("releaseLast exposes suppressed failures when release fails")
    void releaseLastWillThrowWithSuppressedInnerFailuresWhenReleaseFails() {
        TracingReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        try {
            referenceCounted.releaseLast(a);
            fail("releaseLast should throw when owner is not last");
        } catch (IllegalStateException e) {
            assertEquals("net.openhft.chronicle.core.io.TracingReferenceCounted still reserved [INIT]", e.getMessage(),
                    "exception message should indicate resource still reserved after release failure");
            assertEquals("uniqueId main init INIT on main", e.getSuppressed()[0].getMessage().split(" at ")[0], "first suppressed exception should contain INIT reservation details");
            assertEquals("net.openhft.chronicle.core.io.TracingReferenceCounted not reserved by VanillaReferenceOwner{name='a'} closed=false", e.getSuppressed()[1].getMessage(), "second suppressed exception should indicate owner 'a' has no reservation");
        }
    }

    @Test
    @DisplayName("asString includes reference counted details output")
    void asStringWillIncludeReferenceCountedDetails() {
        final TracingReferenceCounted referenceCounted = createReferenceCounted();
        assertTrue(Pattern.matches("TracingReferenceCounted@\\w+ refCount=1", asString(referenceCounted)), "asString output should include class name and refCount");
    }

    @Test
    @DisplayName("asString includes closeable details output text")
    void asStringWillIncludeCloseableDetails() {
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
    @DisplayName("asString renders class name and address")
    void asStringRenderClassNameAndAddressForPojos() {
        class SomePlainObject {

        }
        assertTrue(Pattern.matches("SomePlainObject@\\w+", asString(new SomePlainObject())), "asString output should include class name and memory address for plain objects");
    }

    @Test
    @DisplayName("createdHere returns created stack trace details")
    void createdHereWillReturnCreatedStackTrace() {
        TracingReferenceCounted referenceCounted = createReferenceCounted();

        assertNotNull(referenceCounted.createdHere(), "createdHere should return non-null stack trace");
    }

    @Test
    @DisplayName("reserveTransfer fails when source has no reservation")
    void reserveTransferWillThrowWhenFromHasNoReservation() {
        TracingReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        ReferenceOwner b = ReferenceOwner.temporary("b");

        assertThrows(IllegalStateException.class, () -> referenceCounted.reserveTransfer(a, b),
                "reserveTransfer should fail when source has no reservation");
    }

    @Test
    @DisplayName("reserveTransfer fails when target already reserved")
    void reserveTransferWillThrowWhenToAlreadyHasAReservation() {
        TracingReferenceCounted referenceCounted = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        ReferenceOwner b = ReferenceOwner.temporary("b");
        referenceCounted.reserve(a);
        referenceCounted.reserve(b);

        assertThrows(IllegalStateException.class, () -> referenceCounted.reserveTransfer(a, b),
                "reserveTransfer should fail when target already has a reservation");
    }

    @Test
    @DisplayName("reserve throws when called with self")
    void reserveWillThrowWhenCalledWithSelf() {
        TracingReferenceCounted referenceCounted = createReferenceCounted();

        assertThrows(AssertionError.class, () -> referenceCounted.reserve(referenceCounted),
                "reserve should reject self as reference owner");
    }
}
