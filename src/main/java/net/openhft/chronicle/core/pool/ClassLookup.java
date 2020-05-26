/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.pool;

import org.jetbrains.annotations.NotNull;

public interface ClassLookup {

    @NotNull
    static ClassLookup create(ClassLoader loader) {
        return ClassAliasPool.CLASS_ALIASES.wrap(loader);
    }

    @NotNull
    static ClassLookup create() {
        return ClassAliasPool.CLASS_ALIASES.wrap();
    }

    @NotNull
    default ClassLookup wrap(ClassLoader loader) {
        return new ClassAliasPool(this, loader);
    }

    @NotNull
    default ClassLookup wrap() {
        return new ClassAliasPool(this);
    }

    Class forName(CharSequence name) throws ClassNotFoundException;

    String nameFor(Class clazz);

    void addAlias(Class... classes);

    void addAlias(Class clazz, String names);
}
