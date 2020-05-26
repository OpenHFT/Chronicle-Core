/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.chronicle.core.threads;

@FunctionalInterface
public interface VanillaEventHandler {
    /**
     * perform all tasks once and return ASAP.
     * Called from the event loop's execution thread.
     *
     * @return true if you expect more work very soon.
     * @throws InvalidEventHandlerException when it is not longer valid.
     * Recommended to throw this if your event handler is closed
     */
    boolean action() throws InvalidEventHandlerException, InterruptedException;
}
