/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
