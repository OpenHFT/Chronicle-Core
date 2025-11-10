//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.threads;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HandlerPriorityTest {

    @Test
    void testAliasForPrioritiesWithAliases() {
        assertEquals(HandlerPriority.MEDIUM, HandlerPriority.REPLICATION.alias());
        assertEquals(HandlerPriority.TIMER, HandlerPriority.REPLICATION_TIMER.alias());
        assertEquals(HandlerPriority.MEDIUM, HandlerPriority.CONCURRENT.alias());
    }

    @Test
    void testAliasForPrioritiesWithoutAliases() {
        assertEquals(HandlerPriority.HIGH, HandlerPriority.HIGH.alias());
        assertEquals(HandlerPriority.MEDIUM, HandlerPriority.MEDIUM.alias());
        assertEquals(HandlerPriority.TIMER, HandlerPriority.TIMER.alias());
        assertEquals(HandlerPriority.DAEMON, HandlerPriority.DAEMON.alias());
        assertEquals(HandlerPriority.MONITOR, HandlerPriority.MONITOR.alias());
        assertEquals(HandlerPriority.BLOCKING, HandlerPriority.BLOCKING.alias());
    }
}
