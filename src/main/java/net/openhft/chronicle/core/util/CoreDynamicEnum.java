package net.openhft.chronicle.core.util;

public interface CoreDynamicEnum<E extends CoreDynamicEnum<E>> {
    /**
     * @return unique alias for this object
     */
    String name();

    /**
     * @return unique id for this DynamicEnum or -1 if not set.
     */
    int ordinal();
}
