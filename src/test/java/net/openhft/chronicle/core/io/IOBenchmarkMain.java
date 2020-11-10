package net.openhft.chronicle.core.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public enum IOBenchmarkMain {
    ;

    public static void main(String[] args) throws IOException {
        String path = args.length > 0 ? args[0] : ".";
        File dir = new File(path, "deleteme");
        if (!dir.exists())
            dir.mkdir();
        int count = 0;
        long start = System.nanoTime();
        do {
            try (FileWriter fw = new FileWriter(new File(dir, "file" + count))) {
                fw.write("Hello World");
                count++;
            }
        } while (start + 3e9 > System.nanoTime());
        for (int i = 0; i < count; i++) {
            new File(dir, "file" + i).delete();
        }
        long time = System.nanoTime() - start;
        System.out.printf("IO Throughput %,d IO/s%n",
                (long) (count * 2 * 1e9 / time));
        dir.delete();
    }
}
