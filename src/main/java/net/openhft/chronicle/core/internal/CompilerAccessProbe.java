/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

/** Compiler-only dependencies are isolated here so an ordinary Core startup does not load them. */
public final class CompilerAccessProbe {
    private CompilerAccessProbe() {
    }

    /** Checks the reflective javac operation used by runtime compilation and compiles in memory. */
    public static String check() throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null)
            return "UNAVAILABLE: no system Java compiler; use a JDK with the compiler installed/resolved";
        try (StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null)) {
            if (!System.getProperty("java.specification.version").equals("1.8")) {
                Method method;
                try {
                    method = manager.getClass().getMethod("listLocationsForModules", JavaFileManager.Location.class);
                } catch (NoSuchMethodException missing) {
                    return "UNAVAILABLE: compiler lacks listLocationsForModules: " + missing;
                }
                try {
                    method.setAccessible(true);
                    method.invoke(manager, StandardLocation.valueOf("MODULE_PATH"));
                } catch (RuntimeException denied) {
                    if (denied.getClass().getName().equals("java.lang.reflect.InaccessibleObjectException"))
                        return denied(denied);
                    throw denied;
                } catch (IllegalAccessException denied) {
                    return denied(denied);
                }
            }
            JavaFileObject source = new SimpleJavaFileObject(URI.create("string:///ChronicleAccessCheck.java"), JavaFileObject.Kind.SOURCE) {
                @Override
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    return "final class ChronicleAccessCheck { int value() { return 1; } }";
                }
            };
            JavaFileManager inMemory = new ForwardingJavaFileManager<StandardJavaFileManager>(manager) {
                @Override
                public JavaFileObject getJavaFileForOutput(Location location, String name,
                                                           JavaFileObject.Kind kind, FileObject sibling) {
                    return new SimpleJavaFileObject(URI.create("memory:///" + name + ".class"), kind) {
                        @Override
                        public OutputStream openOutputStream() throws IOException {
                            return new ByteArrayOutputStream();
                        }
                    };
                }
            };
            StringWriter diagnostics = new StringWriter();
            boolean compiled = Boolean.TRUE.equals(compiler.getTask(diagnostics, inMemory, null,
                    Arrays.asList("-proc:none"), null, Collections.singletonList(source)).call());
            return compiled ? "SUPPORTED: javac access and in-memory compilation; Wire reader selection is not checked"
                    : "FAILED: in-memory compilation: " + diagnostics;
        }
    }

    private static String denied(Throwable cause) {
        return "DENIED: javac file-manager access: " + cause +
                "; for classpath runtime compilation add --add-exports jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED " +
                "to this JVM and its compiler-using children; for the full Chronicle classpath options see " +
                "https://github.com/OpenHFT/OpenHFT/blob/ea/docs/Java-Version-Support.adoc";
    }
}
