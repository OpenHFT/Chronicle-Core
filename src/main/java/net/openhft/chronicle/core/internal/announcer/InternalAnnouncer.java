package net.openhft.chronicle.core.internal.announcer;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.announcer.Announcer;
import net.openhft.chronicle.core.pom.PomProperties;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public enum InternalAnnouncer {
    ; // none

    private static final boolean DISABLE_ANNOUNCEMENT = Optional.ofNullable(System.getProperty("chronicle.announcer.disable")).filter("true"::equals).isPresent();
    private static final Consumer<String> LINE_PRINTER = DISABLE_ANNOUNCEMENT ? s -> {
    } : m -> Jvm.startup().on(InternalAnnouncer.class, m);
    private static final AtomicBoolean JVM_ANNOUNCED = new AtomicBoolean();
    private static final Map<String, Set<String>> ANNOUNCED_GROUP_IDS = new ConcurrentHashMap<>();

    public static void announce(@NotNull final String groupId,
                                @NotNull final String artifactId,
                                @NotNull final Map<String, String> properties) {
        if (JVM_ANNOUNCED.compareAndSet(false, true)) {
            announceJvm();
        }
        if (alwaysAnnounce(properties)) {
            announceArtifact(groupId, artifactId, properties);
        } else {
            // Only announce once
            final Set<String> announcedArtifacts = ANNOUNCED_GROUP_IDS.computeIfAbsent(groupId, unused -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
            if (announcedArtifacts.add(artifactId)) {
                announceArtifact(groupId, artifactId, properties);
            }
        }
    }

    private static void announceJvm() {
        LINE_PRINTER.accept(String.format("Running under %s %s with %d processors reported.",
                System.getProperty("java.runtime.name"),
                System.getProperty("java.runtime.version"),
                Runtime.getRuntime().availableProcessors()));
        LINE_PRINTER.accept("Leave your e-mail to get information about the latest releases and patches at https://chronicle.software/release-notes/");
    }

    private static void announceArtifact(@NotNull final String groupId,
                                         @NotNull final String artifactId,
                                         @NotNull final Map<String, String> properties) {
        final Map<String, String> propertiesCopy = new LinkedHashMap<>(properties);
        final String logo = propertiesCopy.remove(Announcer.LOGO);
        if (logo != null) {
            LINE_PRINTER.accept(logo);
        }
        final String version = PomProperties.version(groupId, artifactId);
        final String artifactInfo = String.format("Process id: %d :: %s (%s)", Jvm.getProcessId(), pretty(artifactId), version);
        LINE_PRINTER.accept(artifactInfo);

        final int indent = propertiesCopy.keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);

        final String formatString = "%-"+indent+"s: %s";

        propertiesCopy.entrySet().stream()
                .map(e -> String.format(formatString, e.getKey(), e.getValue()))
                .forEach(LINE_PRINTER);

    }

    // Convert "chronicle-queue" to "Chronicle Queue"
    private static String pretty(@NotNull final String artifactId) {
        final StringBuilder sb = new StringBuilder();
        boolean makeUpperCase = true;
        for (char c:artifactId.toCharArray()) {
            if (makeUpperCase) {
                sb.append(Character.toUpperCase(c));
                makeUpperCase = false;
                continue;
            }
            if ('-' == c) {
                makeUpperCase = true;
                sb.append(' ');
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static boolean alwaysAnnounce(@NotNull final Map<String, String> properties) {
        if (properties.isEmpty())
            return false;
        if (properties.size() == 1)
            // If there is only a "logo" key, then only announce once
            return properties.containsKey("logo");
        return true;
    }
}