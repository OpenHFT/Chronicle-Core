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

public class MockEventHandler implements EventHandler {

    private boolean loopStartedCalled = false;
    private boolean loopFinishedCalled = false;
    private EventLoop eventLoop;

    @Override
    public void eventLoop(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    @Override
    public void loopStarted() {
        loopStartedCalled = true;
    }

    @Override
    public void loopFinished() {
        loopFinishedCalled = true;
    }

    @Override
    public boolean action() {
        return false;
    }

    public boolean isLoopStartedCalled() {
        return loopStartedCalled;
    }

    public boolean isLoopFinishedCalled() {
        return loopFinishedCalled;
    }

    public EventLoop getEventLoop() {
        return eventLoop;
    }
}
