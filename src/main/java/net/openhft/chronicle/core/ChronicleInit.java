package net.openhft.chronicle.core;

/**
 * Handles application code which must be loaded first/run and may override system properties.
 *
 * A {@link Runnable} fully qualified class name may be provided via {@code chronicle.init.runnable} property.
 *
 * This class may also be replaced with different concrete implementation. Code should reside in a static block to
 * be run once. It should contain empty static init() method called to trigger class load.
 */
public final class ChronicleInit {
    public static final String CHRONICLE_INIT_CLASS = "chronicle.init.runnable";

    private ChronicleInit() {
        // Suppresses default constructor, ensuring non-instantiability.
    }

    static {
        // Jvm#getProperty() does not make sense here - not initialized yet
        String initRunnableClass = System.getProperty(CHRONICLE_INIT_CLASS);
        if (initRunnableClass != null && !initRunnableClass.isEmpty()) {
            try {
                Class<? extends Runnable> descendant = (Class<? extends Runnable>) Class.forName(initRunnableClass);
                Runnable chronicleInit = descendant.newInstance();
                chronicleInit.run();
            } catch (Exception ex) {
                // System.err since the logging subsystem may not be up at this point
                ex.printStackTrace();
            }
        }
    }

    static void init() {
        // Should always be left empty
    }
}
