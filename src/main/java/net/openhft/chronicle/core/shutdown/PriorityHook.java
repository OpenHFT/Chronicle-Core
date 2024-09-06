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
 * Collects all hooks that need to be run during shutdown to execute them in a controlled order.
 * <p>
 * This class manages shutdown hooks with specific priorities, ensuring that they are executed
 * in the correct order when the JVM shuts down. It provides functionality to add hooks, prevent duplicate
 * hooks from being registered, and clear all hooks if necessary.
 * </p>
 */
public class PriorityHook {

    private static PriorityHook registeredHook;  // Singleton instance to manage shutdown hooks

    private final TreeMap<Hooklet, Hooklet> hookletPool = new TreeMap<>();  // Map to store hooks based on priority
    private Thread shutdownThread;  // Thread to execute hooks during shutdown

    /**
     * Private constructor to prevent instantiation.
     * Use static methods to interact with the class.
     */
    private PriorityHook() {
    }

    /**
     * Adds a shutdown hook with a specified priority.
     * <p>
     * Prevents adding the same hook (by parameter's class) more than once and returns {@code false} in that case.
     * </p>
     *
     * @param priority The priority of the hook. Hooks with lower values are executed first.
     * @param hook     The function that needs to be run during shutdown.
     * @return {@code true} if the hook was not present and is now added; {@code false} otherwise.
     */
    public static boolean add(int priority, Runnable hook) {
        return addAndGet(Hooklet.of(priority, hook)).equals(hook);
    }

    /**
     * Adds a custom shutdown hook.
     * <p>
     * Prevents adding the same hook (by parameter's class) more than once and returns the existing one in that case.
     * Uses {@link Hooklet#identity()} to check for duplicates.
     * </p>
     *
     * @param hooklet The hook that needs to be run during shutdown in a predictable order.
     * @param <H>     The type of the hooklet extending {@link Hooklet}.
     * @return The {@link Hooklet} instance that will be called during shutdown.
     */
    public static synchronized <H extends Hooklet> H addAndGet(H hooklet) {
        if (registeredHook == null) {
            registeredHook = new PriorityHook();
            // Register shutdown thread with the runtime
            Runtime.getRuntime().addShutdownHook(registeredHook.shutdownThread());
        }

        // Check if the hooklet is already registered
        H registered = (H) registeredHook.hookletPool.get(hooklet);
        if (registered == null) {
            registeredHook.hookletPool.put(hooklet, hooklet);  // Add new hooklet if not already present
            return hooklet;
        }

        return registered;  // Return existing hooklet if already registered
    }

    /**
     * Creates a thread to handle the shutdown process if it hasn't been created yet.
     *
     * @return The shutdown thread.
     */
    private Thread shutdownThread() {
        if (shutdownThread != null)
            return shutdownThread;

        // Initialize the shutdown thread with the onShutdown method
        this.shutdownThread = new Thread(registeredHook::onShutdown);
        return shutdownThread;
    }

    /**
     * Clears all registered shutdown hooks.
     * <p>
     * This method removes the shutdown hook from the runtime and resets the registered hook instance.
     * </p>
     */
    public static synchronized void clear() {
        if (registeredHook != null)
            Runtime.getRuntime().removeShutdownHook(registeredHook.shutdownThread());
        registeredHook = null;  // Reset the registered hook
    }

    /**
     * Executes all registered hooks in order based on their priority.
     * <p>
     * This method is called during the JVM shutdown process to ensure that all hooks are executed in
     * the correct order.
     * </p>
     */
    public void onShutdown() {
        for (Hooklet hooklet : hookletPool.keySet())  // Execute each hook in the order of their priority
            hooklet.onShutdown();
    }

    /**
     * Retrieves the currently registered {@link PriorityHook} instance.
     *
     * @return The registered {@link PriorityHook} instance, or {@code null} if none is registered.
     */
    public static PriorityHook getRegisteredHook() {
        return registeredHook;
    }
}
