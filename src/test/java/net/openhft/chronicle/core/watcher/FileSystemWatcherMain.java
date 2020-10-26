/*
 * Copyright (c) 2016-2020 chronicle.software
 */

package net.openhft.chronicle.core.watcher;

import java.io.File;
import java.io.IOException;

public class FileSystemWatcherMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        FileSystemWatcher fsw = new FileSystemWatcher();
        String absolutePath = new File(".").getAbsolutePath();
        System.out.println("Watching " + absolutePath);
        fsw.addPath(absolutePath);
        ClassifyingWatcherListener listener = new ClassifyingWatcherListener();
        listener.addClassifier(new PlainFileClassifier());
        fsw.addListener(listener);
        fsw.start();
        Thread.currentThread().join();
    }
}
