//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

/**
 * Represents an operation that accepts a single {@code float}-valued argument and returns no result.  This is the
 * primitive type specialization of {@link java.util.function.Consumer} for {@code float}.  Unlike most other functional
 * interfaces, {@code FloatConsumer} is expected to operate via side effects.
 * <p>This is a <a href="package-summary.html">functional interface</a> whose functional method is {@link
 * #accept(float)}.
 *
 * @see java.util.function.Consumer
 * @since 1.8
 */
@FunctionalInterface
public interface FloatConsumer {

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    void accept(float value);

}
