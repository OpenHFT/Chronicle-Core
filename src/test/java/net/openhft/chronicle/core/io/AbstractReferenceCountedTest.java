/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

public class AbstractReferenceCountedTest extends ReferenceCountedTracerContractTest  {

    @Test
    public void reserve() throws IllegalStateException, IllegalArgumentException {
        assumeTrue(Jvm.isResourceTracing());

        MyReferenceCounted rc = createReferenceCounted();
        assertEquals(1, rc.refCount());

        ReferenceOwner a = ReferenceOwner.temporary("a");
        rc.reserve(a);
        assertEquals(2, rc.refCount());

        ReferenceOwner b = ReferenceOwner.temporary("b");
        rc.reserve(b);
        assertEquals(3, rc.refCount());

        try {
            rc.reserve(a);
            fail();
        } catch (IllegalStateException ignored) {
        }
        assertEquals(3, rc.refCount());

        rc.release(b);
        assertEquals(2, rc.refCount());

        rc.release(a);
        assertEquals(1, rc.refCount());
        assertEquals(0, rc.performRelease);

        rc.releaseLast();
        assertEquals(0, rc.refCount());
        assertEquals(1, rc.performRelease);
    }

    @Test
    public void testRefCountIncrement() {
        MyReferenceCounted rc = createReferenceCounted();
        ReferenceOwner a = ReferenceOwner.temporary("a");

        int initialRefCount = rc.refCount();
        rc.reserve(a);

        assertEquals(initialRefCount + 1, rc.refCount());
    }

    @Test
    public void testThreadSafety() throws InterruptedException {
        MyReferenceCounted rc = createReferenceCounted();

        // Simulate being used by a different thread
        Thread thread = new Thread(
                () -> rc.threadSafetyCheck(true));
        thread.start();

        // Give the thread time to start
        thread.join();

        // This should throw an exception as it is being accessed from a different thread
        // This is fine as the other thread has died.
        rc.threadSafetyCheck(true);
    }

    @Test(expected = IllegalStateException.class)
    public void testThreadSafety2() throws InterruptedException {
        MyReferenceCounted rc = createReferenceCounted();

        BlockingQueue q = new LinkedBlockingQueue();
        ReentrantLock lock = new ReentrantLock();
        Condition started = lock.newCondition();
        // Simulate being used by a different thread
        Thread thread = new Thread(
                () -> {
                    rc.threadSafetyCheck(true);
                    q.add("started");
                    Jvm.pause(1000);
                });
        thread.setDaemon(true);
        thread.start();
        q.take();

        // This should throw an exception as it is being accessed from a different thread
        try {
            rc.threadSafetyCheck(true);
        } finally {
            rc.releaseLast();
        }
    }

    @Test
    public void testSingleThreadedCheckReset() {
        MyReferenceCounted rc = createReferenceCounted();

        // Simulate being used by a different thread
        new Thread(() -> rc.threadSafetyCheck(true)).start();

        // Give the thread time to start
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }

        rc.singleThreadedCheckReset();

        // This should not throw an exception because we reset the single-threaded check
        rc.threadSafetyCheck(true);
    }


    @Override
    protected MyReferenceCounted createReferenceCounted() {
        return new MyReferenceCounted();
    }

    static class MyReferenceCounted extends AbstractReferenceCounted {
        int performRelease;

        public MyReferenceCounted() {
        }

        @Override
        protected void performRelease() {
            performRelease++;
        }
    }
}