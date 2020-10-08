package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

public class ThreadLocalPropertiesTest extends CoreTestCommon {

    private Properties properties;

    @Before
    public void save() {
        properties = System.getProperties();
    }

    @After
    public void restore() {
        System.setProperties(properties);
    }

    @Test
    public void forSystemProperties() throws ExecutionException, InterruptedException {
        Properties properties = new Properties(System.getProperties());
        properties.setProperty("a", "none");
        properties.setProperty("b", "none");
        System.setProperties(properties);

        ThreadLocalProperties.forSystemProperties(true);
        ExecutorService es1 = Executors.newSingleThreadExecutor();
        ExecutorService es2 = Executors.newSingleThreadExecutor();
        ExecutorService es3 = Executors.newSingleThreadExecutor();
        es1.submit(() -> System.setProperty("a", "one")).get();
        es2.submit(() -> System.setProperty("b", "two")).get();
        es3.submit(() -> System.setProperty("a", "three")).get();
        es3.submit(() -> System.setProperty("b", "three")).get();
        es1.submit(() -> assertEquals("one", System.getProperty("a"))).get();
        es1.submit(() -> assertEquals("none", System.getProperty("b"))).get();
        es2.submit(() -> assertEquals("none", System.getProperty("a"))).get();
        es2.submit(() -> assertEquals("two", System.getProperty("b"))).get();
        es3.submit(() -> assertEquals("three", System.getProperty("a"))).get();
        es3.submit(() -> assertEquals("three", System.getProperty("b"))).get();
        es1.shutdownNow();
        es2.shutdownNow();
        es3.shutdownNow();
    }
}