//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

/**
 * A marker interface which shows this implementation ignores everything.
 * <p>
 * A caller can assume it doesn't need to call this.
 * <p>
 * Used by {@link  Mocker#ignored(java.lang.Class, java.lang.Class[])}
 */
public interface IgnoresEverything {
}
