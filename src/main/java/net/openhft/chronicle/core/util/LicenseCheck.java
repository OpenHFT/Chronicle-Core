/*
 * Copyright (c) 2016-2019 Chronicle Software Ltd
 */

package net.openhft.chronicle.core.util;


import net.openhft.chronicle.core.Jvm;

import javax.naming.TimeLimitExceededException;
import java.io.Closeable;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.util.Collections;

public enum LicenseCheck {
    ;

    static {
        // ensure loaded first.
        Jvm.debug();
    }

    public static void check(String product, Class caller) {
        String key = System.getProperty(product + ".lic");
        if (key == null || !key.contains("product=" + product + ",")) {
            URL location = caller.getProtectionDomain().getCodeSource().getLocation();
            try {
                FileSystem jarFS = location.getProtocol().equals("file")
                        ? FileSystems.getDefault()
                        : FileSystems.newFileSystem(location.toURI(), Collections.emptyMap());
                try (Closeable c = jarFS == FileSystems.getDefault() ? null : jarFS) {
                    Path resourcePath = jarFS.getPath(location.getPath());
                    FileTime fileTime = Files.getLastModifiedTime(resourcePath);
                    long date = fileTime.toMillis();
                    long time = date - System.currentTimeMillis();
                    long days = time / 86400000 + 92;
                    Jvm.warn().on(LicenseCheck.class, "Evaluation version expires in " + days + " days");
                    if (days < 0)
                        throw new TimeLimitExceededException();
                }
            } catch (Exception e) {
                throw new AssertionError(e);
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
