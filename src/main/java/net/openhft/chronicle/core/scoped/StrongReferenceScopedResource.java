/*
 * Copyright 2016-2025 chronicle.software
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
package net.openhft.chronicle.core.scoped;

import static net.openhft.chronicle.core.io.Closeable.closeQuietly;

/**
 * A {@link ScopedResource} that always keeps a strong reference to its
 * resource. The object therefore remains reachable until
 * {@link #closeResource()} is called. In contrast to
 * {@link WeakReferenceScopedResource}, the reference is never cleared when the
 * scope ends.
 *
 * @param <T> the type of the contained resource
 */
public class StrongReferenceScopedResource<T> extends AbstractScopedResource<T> {

    private final T resource;

    StrongReferenceScopedResource(ScopedThreadLocal<T> scopedThreadLocal, T resource) {
        super(scopedThreadLocal);
        this.resource = resource;
    }

    public T get() {
        return resource;
    }

    @Override
    public void closeResource() {
        closeQuietly(resource);
    }

    @Override
    public Class<?> getType() {
        return resource.getClass();
    }
}
