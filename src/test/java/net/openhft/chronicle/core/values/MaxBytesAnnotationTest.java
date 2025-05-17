package net.openhft.chronicle.core.values;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.junit.jupiter.api.Assertions.*;

class MaxBytesAnnotationTest {

    static class Example {
        void annotatedMethod(@MaxBytes CharSequence text) {
        }
    }

    @Test
    void defaultValueIsSixtyFour() throws NoSuchMethodException {
        Method method = Example.class.getDeclaredMethod("annotatedMethod", CharSequence.class);
        Parameter parameter = method.getParameters()[0];
        MaxBytes annotation = parameter.getAnnotation(MaxBytes.class);
        assertNotNull(annotation, "@MaxBytes annotation should be present");
        assertEquals(64, annotation.value());
    }
}
