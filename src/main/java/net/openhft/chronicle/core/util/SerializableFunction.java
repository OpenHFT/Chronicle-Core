//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import java.io.Serializable;
import java.util.function.Function;

/**
 * This interface is a Function which is also Serializable.
 */
@FunctionalInterface
public interface SerializableFunction<I, O> extends Function<I, O>, Serializable {
}
