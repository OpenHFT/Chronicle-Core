/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @HotMethod} marks a method that is expected to be performance
 * critical. It guides library maintainers and tooling on optimisation targets.
 * The optional value carries a short label such as a hot-count threshold.
 *
 * <p><b>Retention and effect:</b> Retained at runtime with no behavioural
 * impact unless recognised by Chronicle tooling.</p>
 *
 * <pre>
 * {@code @HotMethod("critical polling loop")}
 * void runLoop();
 * </pre>
 *
 * <p>This annotation has no runtime impact but aids Chronicle benchmarking
 * tools.</p>
 *
 * @see ForceInline
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HotMethod {
    @NotNull String value() default "";
}
