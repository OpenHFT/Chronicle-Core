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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AbstractReferenceCountedTest extends ReferenceCountedTracerContractTest {

    @Test
    public void reserve() throws IllegalStateException, IllegalArgumentException {
        Jvm.setResourceTracing(true);

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
