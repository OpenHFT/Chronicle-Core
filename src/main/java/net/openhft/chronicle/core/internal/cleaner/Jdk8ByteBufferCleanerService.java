/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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
package net.openhft.chronicle.core.internal.cleaner;

import net.openhft.chronicle.core.annotation.TargetMajorVersion;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import net.openhft.chronicle.core.internal.util.DirectBufferUtil;

import java.nio.ByteBuffer;

@TargetMajorVersion(majorVersion = 8)
public final class Jdk8ByteBufferCleanerService implements ByteBufferCleanerService {

    @Override
    public void clean(final ByteBuffer buffer) {
        DirectBufferUtil.cleanIfInstanceOfDirectBuffer(buffer);
    }

    @Override
    public Impact impact() {
        return Impact.NO_IMPACT;
    }
}