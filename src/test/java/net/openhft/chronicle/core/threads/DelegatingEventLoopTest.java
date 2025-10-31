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
package net.openhft.chronicle.core.threads;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class DelegatingEventLoopTest {

    private EventLoop innerEventLoop;
    private DelegatingEventLoop delegatingEventLoop;

    @BeforeEach
    void setUp() {
        innerEventLoop = mock(EventLoop.class);
        delegatingEventLoop = new DelegatingEventLoop(innerEventLoop);
    }

    @Test
    void constructorShouldAssignEventLoop() {
        assertEquals(innerEventLoop, delegatingEventLoop.inner);
    }

    @Test
    void nameShouldDelegateToInner() {
        delegatingEventLoop.name();
        verify(innerEventLoop).name();
    }

    @Test
    void startShouldDelegateToInner() {
        delegatingEventLoop.start();
        verify(innerEventLoop).start();
    }

    @Test
    void unpauseShouldDelegateToInner() {
        delegatingEventLoop.unpause();
        verify(innerEventLoop).unpause();
    }

    @Test
    void stopShouldDelegateToInner() {
        delegatingEventLoop.stop();
        verify(innerEventLoop).stop();
    }

    @Test
    void isClosedShouldDelegateToInner() {
        delegatingEventLoop.isClosed();
        verify(innerEventLoop).isClosed();
    }

    @Test
    void isStoppedShouldDelegateToInner() {
        delegatingEventLoop.isStopped();
        verify(innerEventLoop).isStopped();
    }

    @Test
    void isClosingShouldDelegateToInner() {
        delegatingEventLoop.isClosing();
        verify(innerEventLoop).isClosing();
    }

    @Test
    void isAliveShouldDelegateToInner() {
        delegatingEventLoop.isAlive();
        verify(innerEventLoop).isAlive();
    }

    @Test
    void closeShouldDelegateToInner() {
        delegatingEventLoop.close();
        verify(innerEventLoop).close();
    }

    @Test
    void addHandlerShouldDelegateToInner() {
        EventHandler handler = mock(EventHandler.class);
        delegatingEventLoop.addHandler(handler);
        verify(innerEventLoop).addHandler(handler);
    }

    @Test
    void runsInsideCoreLoopShouldDelegateToInner() {
        delegatingEventLoop.runsInsideCoreLoop();
        verify(innerEventLoop).runsInsideCoreLoop();
    }
}
