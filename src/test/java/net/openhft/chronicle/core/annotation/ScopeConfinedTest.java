/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.annotation;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Deprecated(/* to be removed in x.25 as it doesn't test out code */)
class ScopeConfinedTest extends CoreTestCommon {

//    @Test
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
