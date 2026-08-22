/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A lock-free, per-thread cache whose value is held through a {@link Reference} so it
 * can be reclaimed once nothing else refers to it.
 * <p>
 * This is the preferred replacement for
 * {@link ThreadLocalHelper#getTL(ThreadLocal, Supplier)}. That helper takes the value
 * supplier on <em>every</em> call, which encourages callers to allocate a fresh
 * capturing lambda or method reference at each access on the hot path. {@code
 * WeakThreadLocal} instead captures the supplier once, in the constructor, so no
 * per-access garbage is created:
 * <pre>{@code
 * // Allocates a new lambda on every access:
 * private static final ThreadLocal<WeakReference<Buffer>> TL = new ThreadLocal<>();
 * Buffer b = ThreadLocalHelper.getTL(TL, () -> new Buffer(size));
 *
 * // Captures the supplier once; no allocation on access:
 * private static final WeakThreadLocal<Buffer> TL = new WeakThreadLocal<>(() -> new Buffer(size));
 * Buffer b = TL.get();
 * }</pre>
 * <p>
 * The reference strength is configurable. Use {@link ReferenceType#WEAK} (the default)
 * to let the value be collected as soon as it is otherwise unreachable, or
 * {@link ReferenceType#SOFT} to keep it until the JVM is under memory pressure, which
 * can be preferable for expensive-to-rebuild values.
 * <p>
 * The supplier is retained strongly for the lifetime of this {@code WeakThreadLocal}.
 * A supplier that captures an owner therefore also retains that owner and its class
 * loader for the same lifetime.
 *
 * @param <T> the type of value held
 */
public final class WeakThreadLocal<T> {

    /**
     * The strength of the {@link Reference} used to hold the per-thread value.
     */
    public enum ReferenceType {
        /** Value is reclaimable as soon as it is otherwise unreachable. */
        WEAK,
        /** Value is retained until the JVM is under memory pressure. */
        SOFT
    }

    private final ThreadLocal<Reference<T>> threadLocal = new ThreadLocal<>();
    private final Supplier<T> supplier;
    private final ReferenceType referenceType;

    /**
     * Creates a {@code WeakThreadLocal} whose value is held through a
     * {@link WeakReference}.
     *
     * @param supplier used to create the value on first access per thread, and again
     *                 whenever the previous value has been reclaimed; retained strongly
     *                 by this object and must not return {@code null}
     */
    public WeakThreadLocal(@NotNull Supplier<T> supplier) {
        this(supplier, ReferenceType.WEAK);
    }

    /**
     * Creates a {@code WeakThreadLocal} with the given reference strength.
     *
     * @param supplier      used to create the value on first access per thread, and
     *                      again whenever the previous value has been reclaimed; retained
     *                      strongly by this object and must not return {@code null}
     * @param referenceType the strength of the reference used to hold the value
     */
    public WeakThreadLocal(@NotNull Supplier<T> supplier, @NotNull ReferenceType referenceType) {
        this.supplier = Objects.requireNonNull(supplier, "supplier");
        this.referenceType = Objects.requireNonNull(referenceType, "referenceType");
    }

    /**
     * Returns the current thread's value, creating it via the constructor-supplied
     * {@link Supplier} when absent or after it has been reclaimed.
     *
     * @return the current thread's value (never {@code null})
     */
    @NotNull
    public T get() {
        final Reference<T> ref = threadLocal.get();
        T value = ref == null ? null : ref.get();
        if (value == null) {
            value = Objects.requireNonNull(supplier.get(), "supplier returned null");
            threadLocal.set(newReference(value));
        }
        return value;
    }

    /**
     * Removes the current thread's value, if any. The next call to {@link #get()} will
     * recreate it from the supplier.
     */
    public void remove() {
        threadLocal.remove();
    }

    private Reference<T> newReference(T value) {
        return referenceType == ReferenceType.SOFT
                ? new SoftReference<>(value)
                : new WeakReference<>(value);
    }
}
