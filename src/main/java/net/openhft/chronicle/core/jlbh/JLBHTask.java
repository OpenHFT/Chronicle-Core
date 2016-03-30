/*
 * Copyright 2016 higherfrequencytrading.com
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

package net.openhft.chronicle.core.jlbh;

/**
 * Interface for tasks using the JLBH framework.
 */
public interface JLBHTask {
    /**
     * This method is called before the benchmark is started.
     * It gives the task a chance to do initialisation.
     * @param jlbh A reference to the JLBH which is needed so that <code>jlbh.sample()</>
     *             can be invoked when the benchmark is complete. It can also be used to
     *             create more probes into the benchamrk.
     */
    void init(JLBH jlbh);

    /**
     * This method is called for each iteration of the benchmark.
     * The timestamp passed into the method is not the same as <code>System.nanoTime()</>.
     * It is the calculated time that the test is supposed to have started.
     * @param startTimeNS The time that should be used as the start time for the sample.
     */
    void run(long startTimeNS);

    /**
     * This method is used for any clean up that might be required by the benchmark.
     */
    default void complete(){}
}
