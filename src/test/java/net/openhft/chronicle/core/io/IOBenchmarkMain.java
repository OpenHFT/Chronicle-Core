/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import java.io.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public enum IOBenchmarkMain {
    ; // none

    public static void main(String[] args) throws IOException {
        String path = args.length > 0 ? args[0] : ".";
        File dir = new File(path, "deleteme");
        if (!dir.exists() && !dir.mkdir())
            throw new IOException("Unable to create benchmark directory " + dir);
        int count = 0;
        long start = System.nanoTime();
        do {
            File file = new File(dir, "file" + count);
            try (Writer fw = new OutputStreamWriter(new FileOutputStream(file), UTF_8)) {
                fw.write("Hello World");
                count++;
            }
        } while (start + 3e9 > System.nanoTime());
        for (int i = 0; i < count; i++) {
            File f = new File(dir, "file" + i);
            if (!f.delete() && f.exists())
                throw new IOException("Failed to delete benchmark file " + f);
        }
        long time = System.nanoTime() - start;
        System.out.printf("IO Throughput %,d IO/s%n",
                (long) (count * 2 * 1e9 / time));
        if (!dir.delete() && dir.exists())
            throw new IOException("Failed to delete benchmark directory " + dir);
    }
}
