/*
 * Copyright 2016-2025 chronicle.software
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

import static net.openhft.chronicle.core.Jvm.uncheckedCast;

/**
 * Manager for orderly shutdown.
 * <p>
 * Hooklets are stored in a priority ordered map and executed by a single
 * dedicated thread when the JVM terminates. All modifications are
 * synchronised to make the class thread-safe.
 *
 * @apiNote Use unique priorities between {@code 0-100} so that unrelated
 * components do not interfere with each other's shutdown ordering.
 */
public class PriorityHook {
    private static PriorityHook registeredHook;

    private final TreeMap<Hooklet, Hooklet> hookletPool = new TreeMap<>();
    private Thread shutdownThread;

    private PriorityHook() {
    }

    /**
     * Convenience method for adding a simple {@link Runnable}.
     *
     * @param priority value passed to {@link Hooklet#priority()}
     * @param hook     action to run on shutdown
     * @return {@code true} if the hook was newly registered
     * @throws NullPointerException if {@code hook} is {@code null}
     */
    public static boolean add(int priority, Runnable hook) {
        return addAndGet(Hooklet.of(priority, hook)).equals(hook);
    }

    /**
     * Register a custom hooklet.
     *
     * @param hooklet instance to register
     * @param <H>     hooklet subtype
     * @return the hooklet that will be executed
     * @throws NullPointerException if {@code hooklet} is {@code null}
     */
    public static synchronized <H extends Hooklet> H addAndGet(H hooklet) {
        Objects.requireNonNull(hooklet);
        if (registeredHook == null) {
            registeredHook = new PriorityHook();

            Runtime.getRuntime().addShutdownHook(registeredHook.shutdownThread());
        }

        H registered = uncheckedCast(registeredHook.hookletPool.get(hooklet));
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

    /**
     * Remove the registered JVM shutdown hook and discard all hooklets.
     */
    public static synchronized void clear() {
        if (registeredHook != null)
            Runtime.getRuntime().removeShutdownHook(registeredHook.shutdownThread());
        registeredHook = null;
    }

    /**
     * Execute registered hooklets in priority order.
     */
    public void onShutdown() {
        for (Hooklet hooklet : hookletPool.keySet())
            hooklet.onShutdown();
    }

    /**
     * Obtain the singleton managing the shutdown hook, if present.
     *
     * @return current {@code PriorityHook} instance or {@code null}
     */
    public static PriorityHook getRegisteredHook() {
        return registeredHook;
    }
}
