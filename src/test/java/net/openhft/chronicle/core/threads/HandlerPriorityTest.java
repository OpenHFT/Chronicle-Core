/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HandlerPriorityTest {

    @Test
    void testAliasForPrioritiesWithAliases() {
        assertEquals(HandlerPriority.MEDIUM, HandlerPriority.REPLICATION.alias(), "testAliasForPrioritiesWithAliases: L13");
        assertEquals(HandlerPriority.TIMER, HandlerPriority.REPLICATION_TIMER.alias(), "testAliasForPrioritiesWithAliases: L14");
        assertEquals(HandlerPriority.MEDIUM, HandlerPriority.CONCURRENT.alias(), "testAliasForPrioritiesWithAliases: L15");
    }

    @Test
    void testAliasForPrioritiesWithoutAliases() {
        assertEquals(HandlerPriority.HIGH, HandlerPriority.HIGH.alias(), "testAliasForPrioritiesWithoutAliases: L20");
        assertEquals(HandlerPriority.MEDIUM, HandlerPriority.MEDIUM.alias(), "testAliasForPrioritiesWithoutAliases: L21");
        assertEquals(HandlerPriority.TIMER, HandlerPriority.TIMER.alias(), "testAliasForPrioritiesWithoutAliases: L22");
        assertEquals(HandlerPriority.DAEMON, HandlerPriority.DAEMON.alias(), "testAliasForPrioritiesWithoutAliases: L23");
        assertEquals(HandlerPriority.MONITOR, HandlerPriority.MONITOR.alias(), "testAliasForPrioritiesWithoutAliases: L24");
        assertEquals(HandlerPriority.BLOCKING, HandlerPriority.BLOCKING.alias(), "testAliasForPrioritiesWithoutAliases: L25");
    }
}
