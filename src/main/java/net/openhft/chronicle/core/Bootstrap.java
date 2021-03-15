package net.openhft.chronicle.core;

/**
 * Contains the pieces which must be loaded first
 */
enum Bootstrap {
    ;

    public static final String OS_ARCH = System.getProperty("os.arch", "?");
    public static final String VM_VENDOR = System.getProperty("java.vm.vendor");
    public static final String VM_VERSION = System.getProperty("java.vm.version");

    // can't be in Jvm or causes a problem on initialisation.
    static boolean isArm0() {
        return Boolean.parseBoolean(System.getProperty("jvm.isarm")) ||
                OS_ARCH.startsWith("arm") || OS_ARCH.startsWith("aarch");
    }

    static boolean isMacArm0() {
        return System.getProperty("os.name", "?").equals("Mac OS X")
                && OS_ARCH.equals("aarch64");
    }

    static boolean isAzulZing0() {
        final String vendorVersion = VM_VENDOR + VM_VERSION;
        return vendorVersion.matches("Azul .*zing.*$");
    }

    static boolean isAzulZulu0() {
        final String vendorVersion = VM_VENDOR + VM_VERSION;
        System.out.println("vendorVersion: " + vendorVersion);
        return vendorVersion.matches("Azul .*zulu.*$");
    }
}
