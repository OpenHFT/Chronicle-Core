package net.openhft.chronicle.core.annotation;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScopeConfinedTest {

    @Test
    void a() throws NoSuchMethodException {
        Method method = Foo.class.getMethod("stream");
        final Type genericReturnType = method.getGenericReturnType();

        // Not sure how to get the Annotation...
        assertEquals("java.util.stream.Stream<T>", genericReturnType.getTypeName());
    }

    interface Foo<T> {

        // Shows and validates the use of the annotation in a parameter
        void forEach(Consumer<? super @ScopeConfined T> action);

        // Shows and validates the use of the annotation in a return value
        Stream<@ScopeConfined T> stream();

    }

}
