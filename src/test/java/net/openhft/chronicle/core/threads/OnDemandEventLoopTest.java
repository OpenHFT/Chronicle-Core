package net.openhft.chronicle.core.threads;

import org.junit.Test;

import static org.junit.Assert.*;

public class OnDemandEventLoopTest {
    @Test
    public void onDemand() {
        OnDemandEventLoop el = new OnDemandEventLoop(() -> new EventLoop() {
            @Override
            public String name() {
                return "dummy";
            }

            @Override
            public void addHandler(EventHandler handler) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void start() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void unpause() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void stop() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isClosed() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isAlive() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void awaitTermination() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void close() {
            }
        });
        assertFalse(el.hasEventLoop());
        assertEquals("dummy", el.name());
        assertTrue(el.hasEventLoop());
        el.close();
    }
}