/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.testframework.process.JavaProcessBuilder;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class BackgroundResourceReleaserTest extends CoreTestCommon {
    private final AtomicLong closed = new AtomicLong();
    private final AtomicLong released = new AtomicLong();

    // Regression test for issue #344: the releaser thread name should carry
    // application context so a thread dump identifies who started it.
    @Test
    void threadNameCarriesApplicationContext() {
        final String savedCommand = System.getProperty("sun.java.command");
        final String savedApplication = System.getProperty(BackgroundResourceReleaser.APPLICATION_NAME_PROPERTY);
        try {
            System.clearProperty(BackgroundResourceReleaser.APPLICATION_NAME_PROPERTY);
            System.setProperty("sun.java.command", "com.example.MyApp --port 1234");
            assertEquals("com.example.MyApp/" + BackgroundResourceReleaser.BACKGROUND_RESOURCE_RELEASER,
                    BackgroundResourceReleaser.backgroundReleaserThreadName());

            System.setProperty("sun.java.command", "com.example.MyApp\t--port\t1234");
            assertEquals("com.example.MyApp/" + BackgroundResourceReleaser.BACKGROUND_RESOURCE_RELEASER,
                    BackgroundResourceReleaser.backgroundReleaserThreadName());

            System.setProperty(BackgroundResourceReleaser.APPLICATION_NAME_PROPERTY, "quoted application label");
            assertEquals("quoted application label/" + BackgroundResourceReleaser.BACKGROUND_RESOURCE_RELEASER,
                    BackgroundResourceReleaser.backgroundReleaserThreadName());
        } finally {
            restoreProperty("sun.java.command", savedCommand);
            restoreProperty(BackgroundResourceReleaser.APPLICATION_NAME_PROPERTY, savedApplication);
        }
    }

    @Test
    void releaserThreadsFromSeparateClassLoadersHaveDistinctNames() throws Exception {
        final String savedCommand = System.getProperty("sun.java.command");
        final String savedApplication = System.getProperty(BackgroundResourceReleaser.APPLICATION_NAME_PROPERTY);
        final URL classes = BackgroundResourceReleaser.class.getProtectionDomain().getCodeSource().getLocation();
        try (ChildFirstLoader firstLoader = new ChildFirstLoader(classes);
             ChildFirstLoader secondLoader = new ChildFirstLoader(classes)) {
            System.setProperty("sun.java.command", "org.codehaus.plexus.classworlds.launcher.Launcher exec:java");
            System.clearProperty(BackgroundResourceReleaser.APPLICATION_NAME_PROPERTY);

            final LoadedReleaser first = loadReleaser(firstLoader);
            final LoadedReleaser second = loadReleaser(secondLoader);
            try {
                assertNotEquals(first.thread.getName(), second.thread.getName());
                assertTrue(first.thread.getName().endsWith('/' + BackgroundResourceReleaser.BACKGROUND_RESOURCE_RELEASER));
                assertTrue(second.thread.getName().endsWith('/' + BackgroundResourceReleaser.BACKGROUND_RESOURCE_RELEASER));
            } finally {
                first.stop.invoke(null);
                second.stop.invoke(null);
                first.thread.join(1_000);
                second.thread.join(1_000);
                assertFalse(first.thread.isAlive());
                assertFalse(second.thread.isAlive());
            }
        } finally {
            restoreProperty("sun.java.command", savedCommand);
            restoreProperty(BackgroundResourceReleaser.APPLICATION_NAME_PROPERTY, savedApplication);
        }
    }

    private static LoadedReleaser loadReleaser(ClassLoader loader) throws Exception {
        final Class<?> type = Class.forName(BackgroundResourceReleaser.class.getName(), true, loader);
        final Field field = type.getDeclaredField("RELEASER");
        field.setAccessible(true);
        return new LoadedReleaser((Thread) field.get(null), type.getMethod("stop"));
    }

    private static void restoreProperty(String propertyName, String value) {
        if (value == null)
            System.clearProperty(propertyName);
        else
            System.setProperty(propertyName, value);
    }

    private static final class LoadedReleaser {
        private final Thread thread;
        private final Method stop;

        private LoadedReleaser(Thread thread, Method stop) {
            this.thread = thread;
            this.stop = stop;
        }
    }

    private static final class ChildFirstLoader extends URLClassLoader {
        private ChildFirstLoader(URL classes) {
            super(new URL[]{classes}, BackgroundResourceReleaserTest.class.getClassLoader());
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!name.equals(BackgroundResourceReleaser.class.getName()))
                return super.loadClass(name, resolve);

            synchronized (getClassLoadingLock(name)) {
                Class<?> type = findLoadedClass(name);
                if (type == null)
                    type = findClass(name);
                if (resolve)
                    resolveClass(type);
                return type;
            }
        }
    }

    @Test
    void testResourcesCleanedUp() throws IllegalStateException {
        int count = 20;
        for (int i = 0; i < count - 1; i++) {
            new BGCloseable().close();
            new BGReferenceCounted().releaseLast();
        }
        int expectedCount = BackgroundResourceReleaser.BG_RELEASER ? 2 : count;
        assertEquals(expectedCount, closed.get(), 2);
        assertEquals(expectedCount, released.get(), 2);
        BGCloseable bgc = new BGCloseable();
        bgc.close();
        assertTrue(bgc.isClosing());
        assertEquals(!BackgroundResourceReleaser.BG_RELEASER, bgc.isClosed());

        BGReferenceCounted bgr = new BGReferenceCounted();
        bgr.releaseLast();
        assertEquals(0, bgr.refCount());

        long start0 = System.currentTimeMillis();
        WaitingCloseable wc = new WaitingCloseable();
        new Thread(wc::close).start();
        wc.close();
        long time0 = System.currentTimeMillis() - start0;
        int error = Jvm.isAzulZing() || Jvm.isAzulZulu() || Jvm.isMacArm() ? 45 : 20;
        assertBetween(10, time0, 20 + 3 * error);

        BackgroundResourceReleaser.releasePendingResources();
        long time = System.currentTimeMillis() - start0;
        if (BackgroundResourceReleaser.BG_RELEASER) {
            int factor = count * (Jvm.isAzulZulu() || OS.isMacOSX() ? 80 : OS.isWindows() ? 20 : 18);
            assertBetween(count * 9, time, factor);
        }
        assertEquals(count, closed.get());
        assertEquals(count, released.get());
        AbstractCloseable.assertCloseablesClosed();
    }

    @Test
    void testResourcesCleanedUpManually() throws IllegalStateException, InterruptedException {
        Process process = JavaProcessBuilder.create(BackgroundResourceReleaserMain.class)
                .withJvmArguments("-Dbackground.releaser=false").withProgramArguments("manual").start();

        try {
            assertEquals(0, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("BackgroundResourceReleaserMain manual", process);
        }
    }

    @Test
    void testResourcesCleanedUpAndThreadStopped() throws IllegalStateException, InterruptedException {
        Process process = JavaProcessBuilder.create(BackgroundResourceReleaserMain.class).withProgramArguments("stop").start();

        try {
            assertEquals(0, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("BackgroundResourceReleaserMain stop", process);
        }
    }

    @Test
    void testResourcesCleanedUpInForeground() throws IllegalStateException, InterruptedException {
        Process process = JavaProcessBuilder.create(BackgroundResourceReleaserMain.class)
                .withJvmArguments("-Dbackground.releaser=false").withProgramArguments("foreground").start();

        try {
            assertEquals(0, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("BackgroundResourceReleaserMain stop", process);
        }
    }

    @Test
    void isOnBackgroundResourceReleaserThreadIsTrueWhenOnThread() {
        assumeTrue(BackgroundResourceReleaser.BG_RELEASER);
        final WasInBackgroundResourceReleaserRecorder recorder = new WasInBackgroundResourceReleaserRecorder(true);
        recorder.close();
        assertValueBecomes(true, recorder::wasClosedInBackgroundResourceReleaserThread);
        assertTrue(recorder.wasClosedInBackgroundResourceReleaserThread());
    }

    @Test
    void isOnBackgroundResourceReleaserThreadIsFalseWhenNotOnThread() {
        final WasInBackgroundResourceReleaserRecorder recorder = new WasInBackgroundResourceReleaserRecorder(false);
        recorder.close();
        assertValueBecomes(false, recorder::wasClosedInBackgroundResourceReleaserThread);
        assertFalse(recorder.wasClosedInBackgroundResourceReleaserThread());
    }

    private void assertValueBecomes(boolean expectedValue, Supplier<Boolean> supplier) {
        long endTime = System.currentTimeMillis() + 5_000;
        while (supplier.get() == null) {
            Jvm.pause(10);
            if (System.currentTimeMillis() > endTime) {
                fail("Timed out waiting for value");
            }
        }
        assertEquals(expectedValue, supplier.get());
    }

    private static class WasInBackgroundResourceReleaserRecorder extends AbstractCloseable {

        private final boolean shouldPerformCloseInBackground;
        private Boolean wasClosedInBackgroundResourceReleaserThread = null;

        WasInBackgroundResourceReleaserRecorder(boolean shouldPerformCloseInBackground) {
            this.shouldPerformCloseInBackground = shouldPerformCloseInBackground;
        }

        @Override
        protected boolean shouldPerformCloseInBackground() {
            return shouldPerformCloseInBackground;
        }

        @Override
        protected void performClose() {
            wasClosedInBackgroundResourceReleaserThread = BackgroundResourceReleaser.isOnBackgroundResourceReleaserThread();
        }

        Boolean wasClosedInBackgroundResourceReleaserThread() {
            return wasClosedInBackgroundResourceReleaserThread;
        }
    }

    private static void assertBetween(long min, long actual, long max) {
        if (min <= actual && actual <= max)
            return;
        throw new AssertionError("Not in range " + min + " <= " + actual + " <= " + max);
    }

    static class WaitingCloseable extends AbstractCloseable {
        @Override
        protected boolean shouldWaitForClosed() {
            return true;
        }

        @Override
        protected void performClose() {
            Jvm.pause(10);
        }
    }

    class BGCloseable extends AbstractCloseable {
        @Override
        protected boolean shouldPerformCloseInBackground() {
            return true;
        }

        @Override
        protected void performClose() {
            closed.incrementAndGet();
            Jvm.pause(10);
        }
    }

    class BGReferenceCounted extends AbstractReferenceCounted {
        @Override
        protected boolean canReleaseInBackground() {
            return true;
        }

        @Override
        protected void performRelease() {
            released.incrementAndGet();
            Jvm.pause(10);
        }
    }
}
