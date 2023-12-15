package net.openhft.chronicle.core;

import net.openhft.chronicle.core.internal.ChronicleGuarding;
import software.chronicle.internal.InternalApi;

public class JvmMain {

    public static void main(String[] args) throws Exception {
        ChronicleGuarding.bootstrap();
        System.out.println("!!!BOOTSTRAPPED!!!");
        InternalApi.protectedMethod();
    }

     /*public static void main(String[] args) throws Exception {
        URL url = Jvm.class.getClassLoader().getResource("encrypt.bin");
        try (FileSystem fileSystem = FileSystems.newFileSystem(url.toURI(), Collections.emptyMap())) {
            Path resourcePath = fileSystem.getPath("encrypt.bin");
            Files.walk(resourcePath, 2)
                    .forEach(System.out::println);
        }
    }*/
}
