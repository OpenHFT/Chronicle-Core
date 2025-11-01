/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * {@code @RequiredForClient} warns that the annotated class name is baked into
 * on-wire YAML/JSON or a class-alias pool. Its audience is library maintainers
 * reviewing potential breaking changes.
 *
 * <p><b>Retention and effect:</b> Kept in the source only. There is no runtime
 * effect unless build tooling checks for it.</p>
 *
 * <pre>
 * {@code @RequiredForClient("chronicle-queue-dump tool")}
 * public class ApiEntry { }
 * </pre>
 *
 * <p>Renaming or deleting such classes can break clients and should follow
 * semantic-versioning rules.</p>
 */
@Retention(RetentionPolicy.SOURCE)
public @interface RequiredForClient {

    /**
     * Specifies an optional comment to provide additional context or information
     * about where this class is referred to.
     *
     * @return the comment providing more details
     */
    String value() default "";
}
