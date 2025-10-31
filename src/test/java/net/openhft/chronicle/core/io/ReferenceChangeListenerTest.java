/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class ReferenceChangeListenerTest {

    @Test
    public void testOnReferenceAdded() {
        ReferenceChangeListener listener = mock(ReferenceChangeListener.class);
        ReferenceCounted referenceCounted = mock(ReferenceCounted.class);
        ReferenceOwner referenceOwner = mock(ReferenceOwner.class);

        listener.onReferenceAdded(referenceCounted, referenceOwner);

        verify(listener, times(1)).onReferenceAdded(referenceCounted, referenceOwner);
    }

    @Test
    public void testOnReferenceRemoved() {
        ReferenceChangeListener listener = mock(ReferenceChangeListener.class);
        ReferenceCounted referenceCounted = mock(ReferenceCounted.class);
        ReferenceOwner referenceOwner = mock(ReferenceOwner.class);

        listener.onReferenceRemoved(referenceCounted, referenceOwner);

        verify(listener, times(1)).onReferenceRemoved(referenceCounted, referenceOwner);
    }

    @Test
    public void testOnReferenceTransferred() {
        ReferenceChangeListener listener = mock(ReferenceChangeListener.class);
        ReferenceCounted referenceCounted = mock(ReferenceCounted.class);
        ReferenceOwner fromOwner = mock(ReferenceOwner.class);
        ReferenceOwner toOwner = mock(ReferenceOwner.class);

        listener.onReferenceTransferred(referenceCounted, fromOwner, toOwner);

        verify(listener, times(1)).onReferenceTransferred(referenceCounted, fromOwner, toOwner);
    }
}
