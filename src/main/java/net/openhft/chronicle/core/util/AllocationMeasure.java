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

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.Jvm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by daniel on 06/07/2015. A class to measure how much allocation there has been on a
 * thread. Useful in tests to make sure there has been little or even zreo allocation.
 */
public class AllocationMeasure {

    private static final Logger LOG = LoggerFactory.getLogger(AllocationMeasure.class);

    private static final String GET_THREAD_ALLOCATED_BYTES = "getThreadAllocatedBytes";
    private static final String[] SIGNATURE = new String[]{long.class.getName()};
    private final String threadName = Thread.currentThread().getName();
    private final Object[] PARAMS = new Object[]{Thread.currentThread().getId()};
    private MBeanServer mBeanServer;
    private ObjectName name = null;
    private AtomicLong allocated = new AtomicLong();
    private long BYTES_USED_TO_MEASURE = 336;
    private long tid;

    public AllocationMeasure() throws IllegalStateException {
        tid = Thread.currentThread().getId();
        try {
            name = new ObjectName(
                    ManagementFactory.THREAD_MXBEAN_NAME);
            mBeanServer = ManagementFactory.getPlatformMBeanServer();
        } catch (MalformedObjectNameException e) {
            Jvm.warn().on(getClass(), e);
        }

        //calibrate
        for (int i = 0; i < 100; i++) {
            //run a few loops to allow for startup anomalies
            markAllocations();
        }
        long callibrate = threadAllocatedBytes();
        BYTES_USED_TO_MEASURE = threadAllocatedBytes() - callibrate;
        reset();
    }

    public static void main(String[] args) throws IllegalStateException {
        String TEST = "Test";
        AllocationMeasure allocationMeasure = new AllocationMeasure();

        for (int i = 0; i < 1000; i++) {
            allocationMeasure.reset();
            //allocationMeasure = new AllocationMeasure();

            long mark1 = allocationMeasure.markAllocations();

            if (mark1 > 0)
                System.out.println("m1:" + mark1);
        }
        allocationMeasure.printAllocations(TEST);
    }

    public void reset() throws IllegalStateException {
        if (tid != Thread.currentThread().getId())
            throw new AssertionError("AllocationMeasure must not be used over more than 1 thread.");
        allocated.set(threadAllocatedBytes());
    }

    private long threadAllocatedBytes() throws IllegalStateException {
        try {
            return (long) mBeanServer.invoke(
                    name,
                    GET_THREAD_ALLOCATED_BYTES,
                    PARAMS,
                    SIGNATURE
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Calculates the number of bytes allocated since the last reset. Reset is cause by a call to
     * reset(), markAllocations(), printAllocations().
     *
     * @return The number of bytes since the last reset.
     */
    public long markAllocations() throws IllegalStateException {
        if (tid != Thread.currentThread().getId())
            throw new AssertionError("AllocationMeasure must not be used over more than 1 thread.");
        long mark1 = ((threadAllocatedBytes() - BYTES_USED_TO_MEASURE) - allocated.get());
        allocated.set(threadAllocatedBytes());
        return mark1;
    }

    public void printAllocations(CharSequence marker) throws IllegalStateException {
        if (tid != Thread.currentThread().getId())
            throw new AssertionError("AllocationMeasure must not be used over more than 1 thread.");
        long mark1 = ((threadAllocatedBytes() - BYTES_USED_TO_MEASURE) - allocated.get());
        LOG.info(threadName + " allocated " + marker + ":" + mark1);
        allocated.set(threadAllocatedBytes());
    }
}
