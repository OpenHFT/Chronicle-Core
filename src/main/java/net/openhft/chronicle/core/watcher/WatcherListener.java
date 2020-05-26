/*
 * Copyright 2016-2020 Chronicle Software
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
package net.openhft.chronicle.core.watcher;

public interface WatcherListener {
    /**
     * When a file or directory is added or modified.
     * @param filename of the
     * @param modified false is created, true if modified, null if bootstrapping.
     * @throws IllegalStateException when this listener is no longer valid
     */
    void onExists(String base, String filename, Boolean modified) throws IllegalStateException;

    /**
     * Notify that a file or directory was removed.
     * @param filename removed.
     * @throws IllegalStateException when this listener is no longer valid
     */
    void onRemoved(String base, String filename) throws IllegalStateException;
}
