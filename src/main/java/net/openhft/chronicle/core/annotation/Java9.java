//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @Java9} marks a method that is only used when running on Java 9 or
 * later. It is primarily for tooling authors and library maintainers. IDEs may
 * safely delete imports that appear unused when this annotation is present.
 *
 * <p><b>Retention and effect:</b> Present only in the source and discarded by
 * the compiler. It has no runtime effect unless recognised by build tooling.</p>
 *
 * {@code @Java9}
 * void newApiCall();
 *
 * <p>Use {@code Jvm.isJava9Plus()} to guard such calls at runtime.</p>
 *
 * @see TargetMajorVersion
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Java9 {
}
