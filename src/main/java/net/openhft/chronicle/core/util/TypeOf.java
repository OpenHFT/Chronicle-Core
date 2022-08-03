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

package net.openhft.chronicle.core.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeOf<T> {
    private final Type type = extractType();

    public Type type() {
        return type;
    }

    private Type extractType() {
        Type t = getClass().getGenericSuperclass();
        if (!(t instanceof ParameterizedType)) {
            throw new RuntimeException("must specify type parameters");
        }
        ParameterizedType pt = (ParameterizedType) t;
        if (pt.getRawType() != TypeOf.class) {
            throw new RuntimeException("must directly extend TypeOf");
        }
        return pt.getActualTypeArguments()[0];
    }
}
