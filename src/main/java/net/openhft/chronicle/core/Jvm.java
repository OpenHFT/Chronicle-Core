/*
 * Copyright 2015 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;

public enum Jvm {
    ;

    public static final boolean IS_DEBUG = getRuntimeMXBean().getInputArguments().toString().contains("jdwp");

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException rethrow(Throwable t) throws T {
        throw (T) t; // rely on vacuous cast
    }

    public static void checkInterrupted() throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
    }

    public static void trimStackTrace(StringBuilder sb, StackTraceElement... stes) {
        int first = trimFirst(stes);
        int last = trimLast(first, stes);
        for (int i = first; i <= last; i++)
            sb.append("\n\tat ").append(stes[i]);
    }

    public static int trimFirst(StackTraceElement[] stes) {
        int first = 0;
        for (; first < stes.length; first++)
            if (!isInternal(stes[first].getClassName()))
                break;
        return Math.max(0, first - 2);
    }

    public static int trimLast(int first, StackTraceElement[] stes) {
        int last = stes.length - 1;
        for (; first < last; last--)
            if (!isInternal(stes[last].getClassName()))
                break;
        if (last < stes.length - 1) last++;
        return last;
    }

    public static boolean isInternal(String className) {
        return className.startsWith("jdk.") || className.startsWith("sun.") || className.startsWith("java.");
    }

    public static boolean isDebug() {
        return IS_DEBUG;
    }

    public static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
