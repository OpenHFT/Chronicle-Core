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

import net.openhft.chronicle.core.Jvm;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class ClassifyingWatcherListener implements WatcherListener {
    final Set<FileClassifier> classifiers = new CopyOnWriteArraySet<>();
    final Map<Path, FileManager> fileManagerMap = new TreeMap<>();

    @Override
    public void onExists(String base, String filename, Boolean modified) throws IllegalStateException {
        Path path = Paths.get(base, filename);
        if (fileManagerMap.containsKey(path))
            return;
        for (FileClassifier classifier : classifiers) {
            FileManager manager = classifier.classify(base, filename);
            if (manager != null) {
                Jvm.warn().on(getClass(), "File " + base + " " + filename + " classified as " + manager);
                fileManagerMap.put(path, manager);
                manager.start();
            }
        }
    }

    @Override
    public void onRemoved(String base, String filename) throws IllegalStateException {
        Path path = Paths.get(base, filename);
        FileManager info = fileManagerMap.remove(path);
        if (info != null)
            info.stop();
    }

    public void addClassifier(FileClassifier fileClassifier) {
        classifiers.add(fileClassifier);
    }
}
