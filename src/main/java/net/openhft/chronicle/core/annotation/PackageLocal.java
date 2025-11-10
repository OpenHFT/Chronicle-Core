//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * {@code @PackageLocal} marks an element as intentionally package scoped. The
 * audience is library maintainers documenting visibility choices.
 *
 * <p><b>Retention and effect:</b> Retained only in the source and discarded by
 * the compiler. There is no runtime effect unless tooling inspects the source.</p>
 *
 * <pre>
 * {@code @PackageLocal} class Helper { }
 * </pre>
 *
 * <p>Package-private access avoids {@code SecurityManager} checks and can
 * enable sealed hierarchies. Use this annotation to silence IDE warnings such
 * as "access can be private".</p>
 *
 * @see UsedViaReflection
 */
@Documented
@Retention(SOURCE)
@Target({METHOD, FIELD, CONSTRUCTOR, TYPE})
public @interface PackageLocal {
}
