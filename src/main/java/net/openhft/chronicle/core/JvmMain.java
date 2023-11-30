package net.openhft.chronicle.core;

import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class JvmMain {
    public static void main(String[] args) throws Exception {
        URL url = Jvm.class.getClassLoader().getResource("encrypt.bin");
        try (FileSystem fileSystem = FileSystems.newFileSystem(url.toURI(), Collections.emptyMap())) {
            Path resourcePath = fileSystem.getPath("encrypt.bin");
            Files.walk(resourcePath, 2)
                    .forEach(System.out::println);
        }
        ;

    }
}
