package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.Mocker;

/**
 * A marker interface which shows this implementation ignores everything.
 * <p>
 * A caller can assume it doesn't need to call this.
 * <p>
 * Used by {@link  Mocker#ignored(java.lang.Class, java.lang.Class[])}
 */
public interface IgnoresEverything {
}
