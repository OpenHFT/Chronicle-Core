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

package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OnDemandEventLoopTest extends CoreTestCommon {

    private OnDemandEventLoop onDemandEventLoop;

    @Before
    public void setUp() {
        onDemandEventLoop = new OnDemandEventLoop(() -> new EventLoop() {
            @Override
            public String name() {
                return "testEventLoop";
            }

            @Override
            public void addHandler(EventHandler handler) {
                // do nothing
            }

            @Override
            public void start() {
                // do nothing
            }

            @Override
            public void unpause() {
                // do nothing
            }

            @Override
            public void stop() {
                // do nothing
            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public boolean isAlive() {
                return true;
            }

            @Override
            public void awaitTermination() {
                // do nothing
            }

            @Override
            public boolean isStopped() {
                return false;
            }

            @Override
            public void close() {
                // do nothing
            }
        });
    }

    @Test
    public void testHasEventLoop() {
        assertFalse("Initially the event loop should not exist", onDemandEventLoop.hasEventLoop());
        onDemandEventLoop.name(); // this should create the event loop
        assertTrue("After calling name() event loop should exist", onDemandEventLoop.hasEventLoop());
    }

    @Test
    public void testName() {
        assertEquals("Event loop name should be testEventLoop", "testEventLoop", onDemandEventLoop.name());
    }

    @Test
    public void testIsAlive() {
        assertFalse("Event loop should be alive after created", onDemandEventLoop.isAlive());
        assertNotNull(onDemandEventLoop.eventLoop());
        assertTrue("Event loop should be alive after created", onDemandEventLoop.isAlive());
    }

    @Test
    public void testIsClosed() {
        assertFalse("Event loop should not be closed", onDemandEventLoop.isClosed());
        assertNotNull(onDemandEventLoop.eventLoop());
        assertFalse("Event loop should not be closed", onDemandEventLoop.isClosed());
    }

    @Test
    public void testIsStopped() {
        assertFalse("Event loop should not be stopped", onDemandEventLoop.isStopped());
        assertNotNull(onDemandEventLoop.eventLoop());
        assertFalse("Event loop should not be stopped", onDemandEventLoop.isStopped());
    }
}
