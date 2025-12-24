/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JvmLinuxTest extends CoreTestCommon {

    @Test
    void isProcessAliveCommandReturnsTrueWhenPidIsPrinted() {
        assumeTrue(OS.isLinux());
        long pid = 424242;
        boolean alive = Jvm.isProcessAliveCommand(pid,
                Arrays.asList("/bin/sh", "-c", "printf ' " + pid + " '"));
        assertTrue(alive, "process should be reported alive when pid is present");
    }

    @Test
    void isProcessAliveCommandReturnsFalseWhenPidMissing() {
        assumeTrue(OS.isLinux());
        long pid = 111111;
        boolean alive = Jvm.isProcessAliveCommand(pid,
                Arrays.asList("/bin/sh", "-c", "printf ' no-match '"));
        assertFalse(alive, "process should be reported dead when pid is absent");
    }

    @Test
    void isProcessAliveCommandReturnsTrueOnExecFailure() {
        expectException("could be alive due to exception");
        assumeTrue(OS.isLinux());
        long pid = 222222;
        boolean alive = Jvm.isProcessAliveCommand(pid,
                Arrays.asList("/bin/this_command_should_not_exist"));
        assertTrue(alive, "process should be assumed alive when command execution fails");
    }
}
