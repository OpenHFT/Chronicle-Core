/*
 * Copyright (c) 2016-2019 Chronicle Software Ltd
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
