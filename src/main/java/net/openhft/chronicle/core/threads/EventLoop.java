/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.io.Closeable;
import org.jetbrains.annotations.NotNull;


/**
 * Created by peter.lawrey on 22/01/15.
 */
public interface EventLoop extends Closeable {

    void addHandler(boolean dontAttemptToRunImmediatelyInCurrentThread, @NotNull EventHandler handler);

    void addHandler(EventHandler handler);

    void start();

    void unpause();

    void stop();

    /**
     * @return {@code true} close has been called
     */
    boolean isClosed();


    /**
     * @return {@code true} if the main thread is running
     */
    boolean isAlive();

}
