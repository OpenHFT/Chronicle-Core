/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HandlerPriorityTest {

    @DisplayName("testAliasForPrioritiesWithAliases behaviour under expected input and output conditions")
    @Test
    void testAliasForPrioritiesWithAliases() {
        assertEquals(HandlerPriority.MEDIUM, HandlerPriority.REPLICATION.alias(), "REPLICATION priority should alias to MEDIUM");
        assertEquals(HandlerPriority.TIMER, HandlerPriority.REPLICATION_TIMER.alias(), "REPLICATION_TIMER priority should alias to TIMER");
        assertEquals(HandlerPriority.MEDIUM, HandlerPriority.CONCURRENT.alias(), "CONCURRENT priority should alias to MEDIUM");
    }

    @DisplayName("testAliasForPrioritiesWithoutAliases behaviour under expected input and output conditions")
    @Test
    void testAliasForPrioritiesWithoutAliases() {
        assertEquals(HandlerPriority.HIGH, HandlerPriority.HIGH.alias(), "HIGH priority should alias to itself");
        assertEquals(HandlerPriority.MEDIUM, HandlerPriority.MEDIUM.alias(), "MEDIUM priority should alias to itself");
        assertEquals(HandlerPriority.TIMER, HandlerPriority.TIMER.alias(), "TIMER priority should alias to itself");
        assertEquals(HandlerPriority.DAEMON, HandlerPriority.DAEMON.alias(), "DAEMON priority should alias to itself");
        assertEquals(HandlerPriority.MONITOR, HandlerPriority.MONITOR.alias(), "MONITOR priority should alias to itself");
        assertEquals(HandlerPriority.BLOCKING, HandlerPriority.BLOCKING.alias(), "BLOCKING priority should alias to itself");
    }
}
