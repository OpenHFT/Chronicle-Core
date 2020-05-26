/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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
package net.openhft.chronicle.core.watcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class PlainFileManager extends JMXFileManager implements PlainFileManagerMBean {
    private final Path path;

    @Override
    protected String type() {
        return "files";
    }

    public PlainFileManager(String base, String relative, Path path) {
        super(base, relative);
        this.path = path;
    }

    @Override
    public String getFileSize() {
        try {
            long size = Files.size(path);
            if (size < 2 << 10)
                return size + " B";
            if (size < 2 << 20)
                return (size * 10L >> 10) / 10.0 + " KiB";
            return (size * 10L >> 20) / 10.0 + " MiB";
        } catch (IOException e) {
            return e.toString();
        }
    }

    @Override
    public String getContentType() {
        try {
            return Files.probeContentType(path);
        } catch (IOException e) {
            return "Unknown";
        }
    }
}
