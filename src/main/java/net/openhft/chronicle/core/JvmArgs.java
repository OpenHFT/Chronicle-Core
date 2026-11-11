/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.internal.CompilerAccessProbe;

import java.lang.reflect.InvocationTargetException;

/**
 * Opt-in verification of javac access for classpath applications using runtime compilation.
 * This does not verify Wire reader selection or all Chronicle JVM requirements.
 */
public final class JvmArgs {
    private JvmArgs() {
    }

    /**
     * Runs the compiler probe in this JVM. Exit status is 0 for supported, 1 for
     * denied/unavailable/failed, and 2 for invalid command-line usage.
     */
    public static void main(String[] args) {
        if (args.length != 1 || !"--check".equals(args[0])) {
            System.err.println("Usage: net.openhft.chronicle.core.JvmArgs --check");
            System.exit(2);
            return;
        }
        String result = compilerCheck();
        System.err.println(result);
        if (!result.startsWith("SUPPORTED:"))
            System.exit(1);
    }

    // Called once during Jvm initialisation, after resource properties have been loaded.
    static void checkAtStartup() {
        String mode = System.getProperty("chronicle.jvm.args.check", "off");
        if ("off".equals(mode))
            return;
        if (!"warn".equals(mode) && !"fail".equals(mode))
            throw new IllegalArgumentException("chronicle.jvm.args.check must be off, warn or fail: " + mode);
        String result = compilerCheck();
        if (result.startsWith("SUPPORTED:"))
            return;
        // Explicit opt-in diagnostics must remain visible even without an SLF4J provider.
        System.err.println(result);
        if ("fail".equals(mode))
            throw new IllegalStateException(result);
    }

    static String compilerCheck() {
        try {
            // Java 8 has no modules. A probe in Core's named module cannot establish
            // grants to a separately named runtime-compiler module.
            try {
                Object module = Class.class.getMethod("getModule").invoke(JvmArgs.class);
                if ((Boolean) module.getClass().getMethod("isNamed").invoke(module))
                    return "UNAVAILABLE: compiler probe supports classpath deployment only; " +
                            "check access from the runtime compiler's named module";
            } catch (NoSuchMethodException java8) {
                // Java 8 classpath deployment.
            }
            return CompilerAccessProbe.check();
        } catch (LinkageError unavailable) {
            return "UNAVAILABLE: compiler APIs are not available: " + unavailable;
        } catch (Exception failure) {
            Throwable cause = failure instanceof InvocationTargetException ? failure.getCause() : failure;
            return "FAILED: compiler probe could not run: " + cause;
        }
    }
}
