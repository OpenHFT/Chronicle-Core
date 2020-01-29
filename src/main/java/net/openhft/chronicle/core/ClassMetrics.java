package net.openhft.chronicle.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ClassMetrics {
    private final int offset;
    private final int length;

    public ClassMetrics(int offset, int length) {
        this.offset = offset;
        this.length = length;
    }

    public int offset() {
        return offset;
    }

    public int length() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassMetrics that = (ClassMetrics) o;
        return offset == that.offset &&
                length == that.length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, length);
    }

    @Override
    public String toString() {
        return "ClassMetrics{" +
                "offset=" + offset +
                ", length=" + length +
                '}';
    }

    public static void updateJar(final String jarToUpdate, String sourceFile, String fileNameInJar) throws IOException {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");

        URI uri = URI.create("jar:" + new File(jarToUpdate).toURI());

        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
            Path externalFile = new File(sourceFile).toPath();
            Path pathInZipfile = zipfs.getPath(fileNameInJar);
            // copy a file into the zip file
            Files.copy(externalFile, pathInZipfile,
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }



}
