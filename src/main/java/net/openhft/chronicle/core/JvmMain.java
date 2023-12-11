package net.openhft.chronicle.core;

public class JvmMain {
    private static boolean isDecrypted;

    public static void main(String[] args) throws Exception {
        bootstrap();
    }

    public static void bootstrap() {
        if (isDecrypted)
            return;
        try {
            String jarPath = getJarPath();
            System.out.println("Jar path=" + jarPath);
            Class.forName("software.chronicle.Bootstrap");
            Class.forName("software.chronicle.jguard.JGuard")
                    .getMethod("unguard_jar", String.class).invoke(null, jarPath);
            isDecrypted = true;
            System.out.println("!!!BOOTSTRAPPED!!!");
        } catch (ClassNotFoundException cnfe) {
            System.out.printf("Class not found: %s\n", cnfe.getMessage());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String getJarPath() {
        return Jvm.class.getProtectionDomain().getCodeSource().getLocation().getPath();
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
