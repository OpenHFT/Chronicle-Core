//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner.spi;

import java.nio.ByteBuffer;

/**
 * Service interface to perform cleaning operation on ByteBuffers.
 *
 * <p>This interface defines a contract for cleaning memory resources associated with ByteBuffers.
 * It can be implemented by different service providers offering different mechanisms for resource cleanup.
 *
 * <p>Cleaning a ByteBuffer usually means releasing the direct memory that is typically limited
 * and more expensive than regular heap memory. This operation can be critical in environments
 * where lots of direct ByteBuffers are being used, like in high-performance IO or memory-mapped files.
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
     * may have side effects or performance impacts, which can be queried via the {@link #impact()} method.
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
