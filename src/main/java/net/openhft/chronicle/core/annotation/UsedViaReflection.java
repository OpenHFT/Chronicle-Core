/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * {@code @UsedViaReflection} notes that an element is accessed through
 * reflection or tests. It warns library maintainers against removing or hiding
 * the member.
 *
 * <p><b>Retention and effect:</b> Retained in the class file and ignored at
 * runtime unless tooling looks for it.</p>
 *
 * <pre>
 * {@code @UsedViaReflection}
 * public void setAccessible() { }
 * </pre>
 *
 * <p>Typical sources include YAML deserialisation, JMH harnesses and Spring
 * tests. Annotate the member with {@code @SuppressWarnings("unused")} to silence
 * compiler and IDE warnings.</p>
 *
 * <pre>
 * -keep class ** { @UsedViaReflection *; }
 * </pre>
 *
 * @see PackageLocal
 */
@Documented
@Retention(CLASS)
@Target({METHOD, FIELD, CONSTRUCTOR, TYPE})
public @interface UsedViaReflection {
}
