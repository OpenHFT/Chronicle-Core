package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;

public interface SingleThreadedChecked {
    // TODO: remove disable.thread.safety property in x.25
    boolean DISABLE_SINGLE_THREADED_CHECK =
            Jvm.getBoolean("disable.single.threaded.check",
                    Jvm.getBoolean("disable.thread.safety", false));

    /**
     * Forget about previous accesses and only check from now on.
     */
    void singleThreadedCheckReset();

    /**
     * When set to <code>true</code> this resource can be shared between thread provided you ensure they used in a thread safe manner.
     *
     * @param singleThreadedCheckDisabled true to turn off the thread safety check
     */
    void singleThreadedCheckDisabled(boolean singleThreadedCheckDisabled);
}
