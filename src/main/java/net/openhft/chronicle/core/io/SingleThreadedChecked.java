//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;

/**
 * Implement for components that are intended for single-threaded use.  The
 * checks help diagnose accidental sharing between threads.
 */
public interface SingleThreadedChecked {

    /**
     * A flag indicating whether the single-threaded check is disabled.
     * By default, it reads the value from the system property "disable.single.threaded.check",
     * falling back to false if not found.
     * It can be overridden or configured via the system properties.
     */
    boolean DISABLE_SINGLE_THREADED_CHECK =
            Jvm.getBoolean("disable.single.threaded.check", false);

    /**
     * Forget the thread that last used this component. Call when ownership is
     * transferred, for example after constructing an object on one thread and
     * handing it to another.
     */
    void singleThreadedCheckReset();

    /**
     * Sets the flag to disable the single-threaded check.
     * When set to {@code true}, this resource can be shared between threads
     * as long as the users ensure that it is used in a thread-safe manner.
     *
     * @param singleThreadedCheckDisabled {@code true} to turn off the thread safety check,
     *                                    {@code false} to enable it.
     */
    void singleThreadedCheckDisabled(boolean singleThreadedCheckDisabled);
}
