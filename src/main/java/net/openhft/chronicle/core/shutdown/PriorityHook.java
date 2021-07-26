package net.openhft.chronicle.core.shutdown;

import java.util.*;

/**
 * Collects all hook that need to be run during shutdown to execute them in controlled order.
 */
public class PriorityHook {
    private static PriorityHook registeredHook;

    private final PriorityQueue<Hooklet> hooklets = new PriorityQueue<>();
    private final HashMap<Hooklet, Hooklet> hookletPool = new LinkedHashMap<>();

    private PriorityHook() { }

    /**
     * Add a shutdown hook with a specified priority.
     *
     * Will prevent adding the same hook (by parameter's class) more than once, return {@code false} in that case.
     *
     * @param priority See {@link Hooklet#priority()}
     * @param hook Function that needs to be run during shutdown.
     * @return {@code true} if hook was not present and is now added
     */
    public static boolean add(int priority, Runnable hook) {
        return addAndGet(Hooklet.of(priority, hook)).equals(hook);
    }

    /**
     * Add a custom shutdown hook.
     *
     * Will prevent adding the same hook (by parameter's class) more than once, return the existing one in that case.
     * See {@link Hooklet#identity()}.
     *
     * @param hooklet Hook that needs to be run during shutdown in predictable order.
     * @return The {@link Hooklet} instance that will be called during shutdown.
     */
    public static synchronized <H extends Hooklet> H addAndGet(H hooklet) {
        if (registeredHook == null) {
            registeredHook = new PriorityHook();

            Runtime.getRuntime().addShutdownHook(new Thread(registeredHook::onShutdown));
        }

        H registered = (H) registeredHook.hookletPool.get(hooklet);
        if (registered == null) {
            registeredHook.hookletPool.put(hooklet, hooklet);
            registeredHook.hooklets.add(hooklet);

            return hooklet;
        }

        return registered;
    }

    public static synchronized void clear() {
        registeredHook = null;
    }

    public void onShutdown() {
        for (Hooklet hooklet : hooklets)
            hooklet.onShutdown();
    }

    public static PriorityHook getRegisteredHook() {
        return registeredHook;
    }
}
