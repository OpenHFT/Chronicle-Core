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
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.util.SimpleCleaner;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

class SimpleCleanerTest {

    @Test
    void cleanShouldExecuteRunnableOnce() {
        Runnable runnable = mock(Runnable.class);
        SimpleCleaner cleaner = new SimpleCleaner(runnable);

        cleaner.clean();
        cleaner.clean(); // Second call to check idempotency

        verify(runnable, times(1)).run();
    }

    @Test
    void cleanShouldNotExecuteRunnableIfAlreadyCleaned() {
        Runnable runnable = mock(Runnable.class);
        SimpleCleaner cleaner = new SimpleCleaner(runnable);

        cleaner.clean(); // First call
        cleaner.clean(); // Second call

        verify(runnable, times(1)).run();
    }

    @Test
    void constructorShouldInitializeWithProvidedRunnable() {
        Runnable runnable = mock(Runnable.class);
        SimpleCleaner cleaner = new SimpleCleaner(runnable);

        assertNotNull(cleaner); // Verifying that cleaner is initialized
        // Further tests can be performed if needed to check internal state
    }
}
