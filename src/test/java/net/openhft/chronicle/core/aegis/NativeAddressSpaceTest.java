/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.aegis;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NativeAddressSpaceTest {

    @Test
    public void normalizesCommonX8664Aliases() {
        assertEquals("x86_64", NativeAddressSpace.normalizeArch("amd64"));
        assertEquals("x86_64", NativeAddressSpace.normalizeArch("x64"));
        assertEquals("x86_64", NativeAddressSpace.normalizeArch("x86_64"));
    }

    @Test
    public void normalizesCommon32BitAliases() {
        assertEquals("x86_32", NativeAddressSpace.normalizeArch("i686"));
        assertEquals("x86_32", NativeAddressSpace.normalizeArch("x86"));
        assertEquals("arm_32", NativeAddressSpace.normalizeArch("armv7l"));
        assertEquals("arm_32", NativeAddressSpace.normalizeArch("arm32"));
    }

    @Test
    public void usesLowerCanonicalHalfOnX8664() {
        assertEquals(47, NativeAddressSpace.addressBitsFor("amd64", "Linux"));
        assertEquals(47, NativeAddressSpace.addressBitsFor("x86_64", "Windows 11"));
    }

    @Test
    public void uses32BitsForObvious32BitArchitectures() {
        assertEquals(32, NativeAddressSpace.addressBitsFor("i686", "Linux"));
        assertEquals(32, NativeAddressSpace.addressBitsFor("armv7l", "Linux"));
    }

    @Test
    public void keeps56BitDefaultForArm64AndUnknownArchitectures() {
        assertEquals(56, NativeAddressSpace.addressBitsFor("aarch64", "Linux"));
        assertEquals(56, NativeAddressSpace.addressBitsFor("riscv64", "Linux"));
    }
}
