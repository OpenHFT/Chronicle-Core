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
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.Closeable;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileSystemWatcher {
    private final WatchService watchService;
    private final Thread thread = new Thread(this::run, "watcher");

    // shared
    private final Map<WatchKey, PathInfo> watchKeyToPathMap = new ConcurrentHashMap<>();
    private final Set<WatchKey> watchKeysToRemove = new CopyOnWriteArraySet<>();
    private final BlockingQueue<WatcherListener> listenersToAdd = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    // only used by the watcher thread.
    private final List<WatcherListener> listeners = new ArrayList<>();

    public FileSystemWatcher() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
    }

    public void addPath(String directory) {
        addPath(directory, "");
    }
    public void addPath(String base, String relative) {
        Path base0 = Paths.get(base);
        Path base2 = base0.resolve(relative);
        if (Files.isDirectory(base2)) {
            try (Stream<Path> paths = Files.walk(base2, 8, FileVisitOption.FOLLOW_LINKS)) {
                paths.forEach(full -> addPath0(base0, full));

            } catch (IOException e) {
                Jvm.warn().on(FileSystemWatcher.class, "Couldn't walk path " + base, e);
            }
            try {
                bootstrapPath(listeners, base, relative);
            } catch (IOException e) {
                Jvm.warn().on(FileSystemWatcher.class, "Couldn't walk path " + base, e);
            }
        }
    }

    void addPath0(Path base, Path full) {
        if (Files.isDirectory(full)) {
            try {
                String basePath = base.toString();
                watchKeyToPathMap.put(full.register(
                        watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY),
                        new PathInfo(basePath, full.toString()));
            } catch (IOException e) {
                Jvm.warn().on(FileSystemWatcher.class, "Couldn't add path " + full, e);
            }
        }
    }

    public void addListener(WatcherListener listener) {
        listenersToAdd.add(listener);
        thread.interrupt();
    }

    private void removePath(String filename) {
        watchKeyToPathMap.keySet().stream()
                .filter(k -> matches(watchKeyToPathMap.get(k), filename))
                .peek(watchKeysToRemove::add)
                .forEach(WatchKey::cancel);
    }

    private boolean matches(PathInfo path, String filename) {
        String s = path.full;
        return s.equals(filename) || s.startsWith(filename + "/");
    }

    private static String p(String path) {
        return OS.isWindows() ? path.replace('\\', '/') : path;
    }

    void run() {
        WatchKey key;
        while (running) {
            List<WatcherListener> list = new ArrayList<>();
            listenersToAdd.drainTo(list);
            bootstrap(list);
            listeners.addAll(list);

            try {
                if ((key = watchService.take()) == null)
                    break;
                PathInfo base = watchKeyToPathMap.get(key);
                for (WatchEvent<?> event : key.pollEvents()) {
                    for (Iterator<WatcherListener> iterator = listeners.iterator(); iterator.hasNext(); ) {
                        WatcherListener listener = iterator.next();
                        try {
                            if (event.kind() == OVERFLOW) {
                                Jvm.warn().on(getClass(), "Overflow on watcher events for " + base);
                                bootstrap(listeners);
                                continue;
                            }

                            @SuppressWarnings("unchecked")
                            WatchEvent<Path> event2 = (WatchEvent<Path>) event;
                            String fullRelative = (base.relativePath.isEmpty() ? "" : base.relativePath + "/") + event2.context();
                            String filename = base.basePath + "/" + fullRelative;
                            if (event.kind() == ENTRY_CREATE) {
                                listener.onExists(p(base.basePath), p(fullRelative), false);
                                addPath(base.basePath, fullRelative);
                            } else if (event.kind() == ENTRY_MODIFY) {
                                listener.onExists(p(base.basePath), p(fullRelative), true);
                            } else if (event.kind() == ENTRY_DELETE) {
                                listener.onRemoved(p(base.basePath), p(fullRelative));
                                removePath(filename);
                            }

                        } catch (IllegalStateException ise) {
                            iterator.remove();
                        }
                    }
                }
                key.reset();
                if (watchKeysToRemove.contains(key)) {
                    watchKeyToPathMap.remove(key);
                    watchKeysToRemove.remove(key);
                }

            } catch (InterruptedException expected) {
            }
        }
    }

    private void bootstrap(List<WatcherListener> list) {
        for (PathInfo pathInfo : watchKeyToPathMap.values()) {
            try {
                bootstrapPath(list, pathInfo.basePath, "");
            } catch (IOException e) {
                Jvm.warn().on(getClass(), "Failed to walk " + pathInfo, e);
            }
        }

    }

    private void bootstrapPath(List<WatcherListener> list, String base, String relative) throws IOException {
        Path full = Paths.get(base).resolve(relative);
        try (Stream<Path> walk = Files.walk(full, 8, FileVisitOption.FOLLOW_LINKS)) {
            walk.forEach(p -> {
                for (Iterator<WatcherListener> iterator = list.iterator(); iterator.hasNext(); ) {
                    WatcherListener listener = iterator.next();
                    String pToString = p.toString();
                    if (pToString.equals(full.toString()))
                        continue;
                    String filename = pToString.substring(base.length() + 1);
                    try {
                        listener.onExists(p(base), p(filename), null);
                    } catch (IllegalStateException ise) {
                        iterator.remove();
                    }
                }
            });
        }
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        running = false;
        thread.interrupt();
        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Closeable.closeQuietly(watchService);
    }

    static class PathInfo {
        final String basePath;
        final String full;
        final String relativePath;

        public PathInfo(String basePath, String full) {
            this.basePath = basePath;
            this.full = full;
            this.relativePath = basePath.equals(full) ? "" : full.substring(basePath.length()+1);
        }
    }
}
