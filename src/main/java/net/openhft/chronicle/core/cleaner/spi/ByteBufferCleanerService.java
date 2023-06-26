/*
 * Copyright 2016-2020 chronicle.software
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
package net.openhft.chronicle.core.cleaner.spi;

import java.nio.ByteBuffer;

/**
 * Service interface to perform cleaning operation on ByteBuffers.
 *
 * <p>This interface defines a contract for cleaning memory resources associated with ByteBuffers.
 * It can be implemented by different service providers offering different mechanisms for resource cleanup.</p>
 *
 * <p>Cleaning a ByteBuffer usually means releasing the direct memory that is typically limited
 * and more expensive than regular heap memory. This operation can be critical in environments
 * where lots of direct ByteBuffers are being used, like in high-performance IO or memory-mapped files.</p>
 *
 */
public interface ByteBufferCleanerService {

    /**
     * Indicates the impact level of cleaning operation.
     *
     * @return the {@link Impact} of the cleaning operation on performance or resource availability.
     */
    Impact impact();

    /**
     * Performs cleaning operation on the specified ByteBuffer.
     *
     * <p>If the buffer is a direct ByteBuffer, this operation is expected to release
     * any memory resources associated with it. Depending on the implementation, this method
     * may have side effects or performance impacts, which can be queried via the {@link #impact()} method.</p>
     *
     * @param buffer the ByteBuffer to clean.
     */
    void clean(final ByteBuffer buffer);

    /**
     * Enum representing the various impact levels of the cleaning operation.
     * It helps to understand the performance characteristics of the cleaner service.
     *
     * <ul>
     *     <li>NO_IMPACT - Cleaning has no noticeable performance or resource impact.</li>
     *     <li>SOME_IMPACT - Cleaning has some performance or resource impact, but is typically acceptable for most use cases.</li>
     *     <li>UNAVAILABLE - Cleaning operation is unavailable, either due to platform restrictions or other reasons.</li>
     * </ul>
     */
    enum Impact {
        NO_IMPACT,
        SOME_IMPACT,
        UNAVAILABLE
    }
}
