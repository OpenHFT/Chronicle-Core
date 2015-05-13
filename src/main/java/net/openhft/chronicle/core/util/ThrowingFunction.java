package net.openhft.chronicle.core.util;

import java.util.function.Function;

/**
 * Created by peter on 13/05/15.
 */
@FunctionalInterface
public interface ThrowingFunction<I, O, T extends Throwable> {
    O apply(I i) throws T;
}
