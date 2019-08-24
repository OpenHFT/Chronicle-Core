/*
 * Copyright (c) 2016-2019 Chronicle Software Ltd
 */

package net.openhft.chronicle.core.watcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PlainFileClassifier implements FileClassifier {
    @Override
    public FileManager classify(String base, String relative) {
        Path path = Paths.get(base, relative);
        if (Files.isRegularFile(path)) {
            return new PlainFileManager(base, relative, path);
        }
        return null;
    }
}
