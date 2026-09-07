/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.test;

import net.openhft.chronicle.core.threads.EventHandler;
import net.openhft.chronicle.core.threads.EventLoop;

public final class RecordingEventLoop implements EventLoop {
    private final String name;
    private EventHandler lastHandler;
    private boolean alive;
    private boolean stopped;
    private boolean closing;
    private boolean closed;
    private int nameCalls;
    private int addHandlerCalls;
    private int startCalls;
    private int unpauseCalls;
    private int stopCalls;
    private int isAliveCalls;
    private int isStoppedCalls;
    private int isClosingCalls;
    private int isClosedCalls;
    private int closeCalls;
    private int runsInsideCoreLoopCalls;

    public RecordingEventLoop() {
        this("recording-event-loop");
    }

    public RecordingEventLoop(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        nameCalls++;
        return name;
    }

    @Override
    public void addHandler(EventHandler handler) {
        addHandlerCalls++;
        lastHandler = handler;
    }

    @Override
    public void start() {
        startCalls++;
        alive = true;
    }

    @Override
    public void unpause() {
        unpauseCalls++;
    }

    @Override
    public void stop() {
        stopCalls++;
        stopped = true;
        alive = false;
    }

    @Override
    public boolean isAlive() {
        isAliveCalls++;
        return alive;
    }

    @Override
    public boolean isStopped() {
        isStoppedCalls++;
        return stopped;
    }

    @Override
    public void close() {
        closeCalls++;
        closing = true;
        closed = true;
    }

    @Override
    public boolean isClosing() {
        isClosingCalls++;
        return closing || closed;
    }

    @Override
    public boolean isClosed() {
        isClosedCalls++;
        return closed;
    }

    @Override
    public boolean runsInsideCoreLoop() {
        runsInsideCoreLoopCalls++;
        return true;
    }

    public EventHandler lastHandler() {
        return lastHandler;
    }

    public int nameCalls() {
        return nameCalls;
    }

    public int addHandlerCalls() {
        return addHandlerCalls;
    }

    public int startCalls() {
        return startCalls;
    }

    public int unpauseCalls() {
        return unpauseCalls;
    }

    public int stopCalls() {
        return stopCalls;
    }

    public int isAliveCalls() {
        return isAliveCalls;
    }

    public int isStoppedCalls() {
        return isStoppedCalls;
    }

    public int isClosingCalls() {
        return isClosingCalls;
    }

    public int isClosedCalls() {
        return isClosedCalls;
    }

    public int closeCalls() {
        return closeCalls;
    }

    public int runsInsideCoreLoopCalls() {
        return runsInsideCoreLoopCalls;
    }
}
