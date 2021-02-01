package net.openhft.chronicle.core;

/**
 * Contains the pieces which must be loaded first
 */
enum Bootstrap {
    ;

    // can't be in Jvm or causes a problem on initialisation.
    static boolean isArm0() {
        return Boolean.parseBoolean(System.getProperty("jvm.isarm")) ||
                System.getProperty("os.arch", "?").startsWith("arm") || System.getProperty("os.arch", "?").startsWith("aarch");
    }
}
