/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.aegis;

import java.util.Locale;

/**
 * Shared assumptions and helpers for Chronicle's native-address validation.
 *
 * <p>The current model is intentionally conservative: it treats native
 * addresses as belonging to a bounded user-space range and reserves the first
 * 64 KiB so null-adjacent addresses are never considered dereferenceable. This
 * gives {@link MemoryAegis} one consistent notion of "plausible native
 * address" while still leaving room for small architecture-specific tweaks in
 * the lazy holder.</p>
 *
 * <p>The key bounds live in {@link BoundsHolder} so they are initialized only
 * if a native-address check is actually executed. Chronicle often compiles
 * assertion-based checks out of hot paths, so deferring these values keeps the
 * no-check path free from unnecessary initialization work and leaves room for
 * more expensive architecture-specific discovery in a later revision.</p>
 */
public final class NativeAddressSpace {

    private NativeAddressSpace() {
    }

    /**
     * Returns the lowest address considered dereferenceable by the current
     * native-address model.
     *
     * @return the inclusive lower bound for native addresses
     */
    public static long minAddress() {
        return BoundsHolder.MIN_ADDRESS;
    }

    /**
     * Returns the exclusive upper bound for the current native-address model.
     *
     * @return the exclusive upper bound for native addresses
     */
    public static long maxAddressExclusive() {
        return BoundsHolder.MAX_ADDRESS_EXCLUSIVE;
    }

    /**
     * Returns the inclusive upper bound for the current native-address model.
     * Because the underlying value is {@code 2^N - 1}, this doubles as a
     * bitmask: {@code (value & maxAddressInclusive()) == value} is true
     * exactly when {@code value} is in {@code [0, maxAddressExclusive())}.
     *
     * @return the inclusive upper bound for native addresses
     */
    public static long maxAddressInclusive() {
        return BoundsHolder.MAX_ADDRESS_INCLUSIVE;
    }

    /**
     * Resolves the address-width assumption for the supplied platform strings.
     *
     * <p>The current policy intentionally tweaks only the simplest and most
     * defensible cases:</p>
     *
     * <ul>
     * <li>x86-64 is capped to the lower canonical half ({@code 2^47})</li>
     * <li>obvious 32-bit architecture aliases are capped to {@code 2^32}</li>
     * <li>everything else keeps the existing 56-bit default</li>
     * </ul>
     *
     * <p>AArch64 deliberately stays on the 56-bit default for now. A tighter
     * arm64 policy needs tag-aware handling rather than just a smaller ceiling,
     * so this helper avoids pretending to solve that problem early.</p>
     *
     * @param rawArch the {@code os.arch} value
     * @param rawOsName the {@code os.name} value, reserved for future tuning
     * @return the assumed number of address bits for the platform
     */
    static int addressBitsFor(final String rawArch,
                              final String rawOsName) {
        switch (normalizeArch(rawArch)) {
            case "x86_64":
                return 47;

            case "x86_32":
            case "arm_32":
                return 32;

            default:
                return 56;
        }
    }

    /**
     * Normalizes common {@code os.arch} aliases into a small set of policy
     * buckets used by {@link #addressBitsFor(String, String)}.
     *
     * @param rawArch the raw architecture string
     * @return the normalized architecture bucket
     */
    static String normalizeArch(final String rawArch) {
        final String arch = rawArch == null ? "" : rawArch.toLowerCase(Locale.ROOT).trim();
        switch (arch) {
            case "amd64":
            case "x86_64":
            case "x64":
                return "x86_64";

            case "x86":
            case "i386":
            case "i486":
            case "i586":
            case "i686":
                return "x86_32";

            case "arm":
            case "arm32":
            case "armv6":
            case "armv6l":
            case "armv7":
            case "armv7l":
                return "arm_32";

            default:
                return arch;
        }
    }

    /**
     * Lazy holder for the default native-address bounds.
     */
    static final class BoundsHolder {
        static final long MIN_ADDRESS = 64L << 10;
        static final int ADDRESS_BITS = addressBitsFor(
                System.getProperty("os.arch", ""),
                System.getProperty("os.name", ""));
        static final long MAX_ADDRESS_EXCLUSIVE = 1L << ADDRESS_BITS;
        static final long MAX_ADDRESS_INCLUSIVE = MAX_ADDRESS_EXCLUSIVE - 1;

        private BoundsHolder() {
        }
    }
}
