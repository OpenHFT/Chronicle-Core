/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public enum IOBenchmarkMain {
    ; // none

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
