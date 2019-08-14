/*
 * Copyright (c) 2016-2019 Chronicle Software Ltd
 */

package net.openhft.chronicle.core.util;


import net.openhft.chronicle.core.Jvm;

import javax.naming.TimeLimitExceededException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public enum LicenseCheck {
    ;

    public static final String CHRONICLE_LICENSE = "chronicle.license";

    static {
        // ensure loaded first.
        Jvm.debug();
    }

    public static void check(String product, Class caller) {
        String key = System.getProperty(CHRONICLE_LICENSE);
        if (key == null || !key.contains("product=" + product + ",")) {
            try {
                URL location = caller.getProtectionDomain().getCodeSource().getLocation();
                String path = location.getPath();
                long date;
                if (path.endsWith(".jar")) {
                    try (JarFile jarFile = new JarFile(path)) {
                        final Enumeration<JarEntry> entries = jarFile.entries();
                        FileTime fileTime = null;
                        while (entries.hasMoreElements()) {
                            final JarEntry entry = entries.nextElement();
                            fileTime = entry.getLastModifiedTime();
                            if (fileTime != null)
                                break;
                        }
                        date = fileTime.toMillis();
                    }
                } else {
                    File file = new File(path + caller.getName().replace(".", "/") + ".class");
                    date = file.lastModified();
                }
                long time = date - System.currentTimeMillis();
                long days = time / 86400000 + 92;
                Jvm.warn().on(LicenseCheck.class, "Evaluation version expires in " + days + " days");
                if (days < 0)
                    throw new AssertionError(new TimeLimitExceededException());

            } catch (IOException e) {
                Jvm.warn().on(LicenseCheck.class, "Evaluation version expires in 1 day");
            }
        } else {
            int start = key.indexOf("expires=") + 8;
            int end = key.indexOf(",", start);
            LocalDate date = LocalDate.parse(key.substring(start, end));
            int start2 = key.indexOf("owner=") + 6;
            int end2 = key.indexOf(",", start2);
            String owner = key.substring(start2, end2);
            long days = date.toEpochDay() - System.currentTimeMillis() / 86400000;
            Jvm.warn().on(LicenseCheck.class, "License for " + owner + " expires in " + days + " days");
            if (days < 0)
                throw new AssertionError(new TimeLimitExceededException());
        }
    }
}
