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

package net.openhft.chronicle.core.scoped;

import static net.openhft.chronicle.core.io.Closeable.closeQuietly;

/**
 * A {@link ScopedResource} implementation that always retains a strong reference to the
 * contained resource, even when not "in use".
 * <p>
 * This class ensures that the resource it holds is not garbage collected as long as the
 * {@link StrongReferenceScopedResource} itself is reachable. This is useful for managing
 * resources that need to persist in memory across multiple uses within a single thread scope.
 *
 * @param <T> The type of the contained resource
 */
public class StrongReferenceScopedResource<T> extends AbstractScopedResource<T> {

    private final T resource; // The strongly referenced resource

    /**
     * Constructs a new {@code StrongReferenceScopedResource} with the specified resource.
     *
     * @param scopedThreadLocal The {@link ScopedThreadLocal} managing this resource.
     * @param resource          The resource to be strongly referenced.
     */
    StrongReferenceScopedResource(ScopedThreadLocal<T> scopedThreadLocal, T resource) {
        super(scopedThreadLocal);
        this.resource = resource;
    }

    /**
     * Returns the strongly referenced resource.
     *
     * @return The resource contained in this {@link StrongReferenceScopedResource}.
     */
    public T get() {
        return resource;
    }

    /**
     * Closes the contained resource quietly.
     * <p>
     * This method is called when the resource is no longer needed and should be
     * released back to the "pool". It uses {@link Closeable#closeQuietly} to handle
     * any exceptions that may occur during the closing process, ensuring that exceptions
     * do not propagate.
     */
    @Override
    public void closeResource() {
        closeQuietly(resource);
    }

    /**
     * Returns the class type of the contained resource.
     *
     * @return The {@link Class} type of the resource.
     */
    @Override
    public Class<?> getType() {
        return resource.getClass();
    }
}
