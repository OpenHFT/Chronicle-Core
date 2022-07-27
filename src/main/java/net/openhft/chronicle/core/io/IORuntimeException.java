/*
 * Copyright 2016-2020 chronicle.software
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

package net.openhft.chronicle.core.io;

import java.net.SocketException;

/**
 * A RuntimeException triggered when a underlying IO resource throws an exception.
 */
public class IORuntimeException extends RuntimeException {
    public IORuntimeException(String message) {
        super(message);
    }

    public IORuntimeException(Throwable thrown) {
        super(thrown);
    }

    public IORuntimeException(String message, Throwable thrown) {
        super(message, thrown);
    }

    public static IORuntimeException newIORuntimeException(Exception e) {
        return IOTools.isClosedException(e)
                ? new ClosedIORuntimeException("Closed", e)
                : new IORuntimeException(e);
    }
}