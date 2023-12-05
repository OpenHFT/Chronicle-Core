package net.openhft.chronicle.core;

public class JvmMain {
    private static boolean isDecrypted;

    public static void main(String[] args) throws Exception {
        Jvm.bootstrap();
    }

    public static void bootstrap() {
        if (isDecrypted)
            return;
        try {
            String jarPath = getJarPath();
            System.out.println("Jar path=" + jarPath);
            Class.forName("software.chronicle.jguard.JGuard")
                    .getMethod("unguard_jar", String.class).invoke(null, jarPath);
        } catch (ClassNotFoundException cnfe) {
            // No-op.
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String getJarPath() {
        String path = Jvm.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (OS.isWindows() && path.startsWith("/")) // "/C:/Program" is not a valid Path
            path = path.substring(1);
        return path;
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
