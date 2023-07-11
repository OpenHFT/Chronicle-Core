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

package net.openhft.chronicle.core.shutdown;

import java.util.*;

/**
 * Collects all hook that need to be run during shutdown to execute them in controlled order.
 */
public class PriorityHook {
    private static PriorityHook registeredHook;

    private final TreeMap<Hooklet, Hooklet> hookletPool = new TreeMap<>();
    private Thread shutdownThread;

    private PriorityHook() {
    }

    /**
     * Add a shutdown hook with a specified priority.
     * <p>
     * Will prevent adding the same hook (by parameter's class) more than once, return {@code false} in that case.
     *
     * @param priority See {@link Hooklet#priority()}
     * @param hook     Function that needs to be run during shutdown.
     * @return {@code true} if hook was not present and is now added
     */
    public static boolean add(int priority, Runnable hook) {
        return addAndGet(Hooklet.of(priority, hook)).equals(hook);
    }

    /**
     * Add a custom shutdown hook.
     * <p>
     * Will prevent adding the same hook (by parameter's class) more than once, return the existing one in that case.
     * See {@link Hooklet#identity()}.
     *
     * @param hooklet Hook that needs to be run during shutdown in predictable order.
     * @return The {@link Hooklet} instance that will be called during shutdown.
     */
    public static synchronized <H extends Hooklet> H addAndGet(H hooklet) {
        if (registeredHook == null) {
            registeredHook = new PriorityHook();

            Runtime.getRuntime().addShutdownHook(registeredHook.shutdownThread());
        }

        H registered = (H) registeredHook.hookletPool.get(hooklet);
        if (registered == null) {
            registeredHook.hookletPool.put(hooklet, hooklet);

            return hooklet;
        }

        return registered;
    }

    private Thread shutdownThread() {
        if (shutdownThread != null)
            return shutdownThread;

        this.shutdownThread = new Thread(registeredHook::onShutdown);

        return shutdownThread;
    }

    public static synchronized void clear() {
        if (registeredHook != null)
            Runtime.getRuntime().removeShutdownHook(registeredHook.shutdownThread());
        registeredHook = null;
    }

    public void onShutdown() {
        for (Hooklet hooklet : hookletPool.keySet())
            hooklet.onShutdown();
    }

    public static PriorityHook getRegisteredHook() {
        return registeredHook;
    }
}
