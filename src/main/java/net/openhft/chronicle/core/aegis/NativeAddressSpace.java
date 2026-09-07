/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.aegis;

import net.openhft.chronicle.core.annotation.Address;
import net.openhft.chronicle.core.annotation.NonNegative;
import net.openhft.chronicle.core.internal.Bootstrap;

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
 * <p>This model intentionally excludes negative or sign-extended special
 * mappings such as the legacy x86-64 {@code [vsyscall]} mapping visible in
 * {@code /proc/self/maps}. In other words, the configured range is a
 * conservative "ordinary user-space" envelope, not a complete model of every
 * mapping the OS might expose.</p>
 *
 * <p>The key bounds live in {@link BoundsHolder} so they are initialized only
 * if a native-address check is actually executed. Chronicle often compiles
 * assertion-based checks out of hot paths, so deferring these values keeps the
 * no-check path free from unnecessary initialization work and leaves room for
 * more expensive architecture-specific discovery in a later revision.</p>
 *
 * <p><b>Inclusive vs exclusive bounds:</b> the address space is modeled as the
 * half-open interval {@code [minAddress(), maxAddressExclusive())}. Error
 * messages reporting the upper bound use {@link #maxAddressInclusive()} (the
 * highest address a caller may legally pass); arithmetic wrap-around checks
 * use {@link #maxAddressExclusive()} so {@code address + length} never
 * crosses the ceiling. Do not "unify" the two forms -- they are deliberately
 * different representations of the same ceiling.</p>
 *
 * <p><b>Testability of the holder bounds:</b> {@link BoundsHolder#ADDRESS_BITS}
 * is resolved once from {@link Bootstrap#OS_ARCH} at class initialisation and
 * cannot be re-selected for the lifetime of the JVM. Tests that want to cover
 * a specific address-width policy should drive the pure helper
 * {@link #addressBitsFor(String)} directly; tests that merely pin the
 * invariant (for example, that {@link #maxAddressExclusive()} equals
 * {@code 1L << ADDRESS_BITS}) may read the holder fields freely.</p>
 */
final class NativeAddressSpace {

    private NativeAddressSpace() {
    }

    /**
     * Returns the lowest address considered dereferenceable by the current
     * native-address model.
     *
     * @return the inclusive lower bound for native addresses
     */
    @Address
    static long minAddress() {
        return BoundsHolder.MIN_ADDRESS;
    }

    /**
     * Returns the exclusive upper bound for the current native-address model.
     *
     * @return the exclusive upper bound for native addresses
     */
    static long maxAddressExclusive() {
        return BoundsHolder.MAX_ADDRESS_EXCLUSIVE;
    }

    /**
     * Returns the inclusive upper bound for the current native-address model.
     *
     * @return the inclusive upper bound for native addresses
     */
    @Address
    static long maxAddressInclusive() {
        return BoundsHolder.MAX_ADDRESS_INCLUSIVE;
    }

    /**
     * Returns {@code true} when {@code baseOffset + length} would cross
     * {@link #maxAddressExclusive()}. The subtraction form avoids the
     * signed-overflow hazard of comparing {@code baseOffset + length} against
     * the ceiling directly.
     *
     * <p>The first parameter is deliberately labelled generically: callers
     * pass a native address ({@link MemoryAegis#assertAddressRange(long, long)})
     * or an object-relative offset
     * ({@link MemoryAegis#assertObjectRange(Object, long, long)}); both must
     * stay below the same configured ceiling.</p>
     *
     * @param baseOffset the starting address or object-relative offset
     * @param length     the byte count covered, must be non-negative
     * @return {@code true} when the range would exceed the configured ceiling
     */
    static boolean wouldOverflow(@NonNegative final long baseOffset,
                                 @NonNegative final long length) {
        return length > BoundsHolder.MAX_ADDRESS_EXCLUSIVE - baseOffset;
    }

    /**
     * Resolves the address-width assumption for the supplied platform string.
     *
     * <p>Only explicit 32-bit architecture aliases ({@code x86_32}, {@code arm_32})
     * are capped to {@code 2^32}; every other platform - including x86-64 and
     * arm64 - uses the 56-bit default. The broader ceiling is the safer
     * choice: some machines expose less than 2^56 of user-space addresses,
     * while modern x86-64 configurations (Linear Address Masking, 5-level
     * paging) can exceed 2^47 and a tighter x86-64 cap would reject those
     * legitimate addresses as bogus. A tag-aware arm64 policy is deferred
     * until the rest of the stack is ready for it.</p>
     *
     * @param rawArch the {@code os.arch} value
     * @return the assumed number of address bits for the platform
     */
    static int addressBitsFor(final String rawArch) {
        switch (normalizeArch(rawArch)) {
            case "x86_32":
            case "arm_32":
                return 32;

            default:
                return 56;
        }
    }

    /**
     * Normalizes 32-bit {@code os.arch} aliases into the buckets that
     * {@link #addressBitsFor(String)} recognises. Anything else - x86-64,
     * arm64, riscv64, ... - is returned in lower-case trimmed form and falls
     * through to the 56-bit default.
     *
     * @param rawArch the raw architecture string
     * @return the normalized architecture bucket
     */
    static String normalizeArch(final String rawArch) {
        final String arch = rawArch == null ? "" : rawArch.toLowerCase(Locale.ROOT).trim();
        switch (arch) {
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
        static final int ADDRESS_BITS = addressBitsFor(Bootstrap.OS_ARCH);
        static final long MAX_ADDRESS_EXCLUSIVE = 1L << ADDRESS_BITS;
        static final long MAX_ADDRESS_INCLUSIVE = MAX_ADDRESS_EXCLUSIVE - 1;

        private BoundsHolder() {
        }
    }
}
