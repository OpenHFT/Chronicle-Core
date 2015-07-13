package net.openhft.chronicle.core.util;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Created by peter.lawrey on 11/07/2015.
 */
public interface SerializableConsumer<T> extends Consumer<T>, Serializable {
}
