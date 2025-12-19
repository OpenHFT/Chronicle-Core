/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class JvmLinuxTest extends CoreTestCommon {

    @Test
    public void isProcessAliveCommandReturnsTrueWhenPidIsPrinted() {
        assumeTrue(OS.isLinux());
        long pid = 424242;
        boolean alive = Jvm.isProcessAliveCommand(pid,
                Arrays.asList("/bin/sh", "-c", "printf ' " + pid + " '"));
        assertTrue(alive);
    }

    @Test
    public void isProcessAliveCommandReturnsFalseWhenPidMissing() {
        assumeTrue(OS.isLinux());
        long pid = 111111;
        boolean alive = Jvm.isProcessAliveCommand(pid,
                Arrays.asList("/bin/sh", "-c", "printf ' no-match '"));
        assertFalse(alive);
    }

    @Test
    public void isProcessAliveCommandReturnsTrueOnExecFailure() {
        expectException("could be alive due to exception");
        assumeTrue(OS.isLinux());
        long pid = 222222;
        boolean alive = Jvm.isProcessAliveCommand(pid,
                Arrays.asList("/bin/this_command_should_not_exist"));
        assertTrue(alive);
    }
}
