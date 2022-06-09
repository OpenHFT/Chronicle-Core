package net.openhft.chronicle.core.io;

public interface SingleThreadedChecked {
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
