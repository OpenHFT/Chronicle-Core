package net.openhft.chronicle.core.util;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * Created by peter.lawrey on 11/07/2015.
 */
@FunctionalInterface
public interface SerializablePredicate<T> extends Predicate<T>, Serializable {
}
