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

package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;

public interface SingleThreadedChecked {
    // TODO: remove disable.thread.safety property in x.25
    boolean DISABLE_SINGLE_THREADED_CHECK =
            Jvm.getBoolean("disable.single.threaded.check",
                    Jvm.getBoolean("disable.thread.safety", false));

    /**
     * Forget about previous accesses and only check from now on.
     */
    void singleThreadedCheckReset();

    /**
     * When set to <code>true</code> this resource can be shared between thread provided you ensure they used in a thread safe manner.
     *
     * @param singleThreadedCheckDisabled true to turn off the thread safety check
     */
    void singleThreadedCheckDisabled(boolean singleThreadedCheckDisabled);
}
