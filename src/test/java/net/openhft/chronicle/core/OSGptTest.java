package net.openhft.chronicle.core;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OSGptTest {
    @Test
    public void testFindTmp() {
        String tmpDirectory = OS.findTmp();
        assertNotNull(tmpDirectory);
        assertTrue(new File(tmpDirectory).isDirectory());
        assertTrue(new File(tmpDirectory).canWrite());
    }

    @Test
    public void testFindDir() throws  FileNotFoundException {
        String dir = OS.findDir("test-classes");
        assertNotNull(dir);
        assertTrue(new File(dir).isDirectory());
        assertTrue(dir.endsWith("test-classes"));
    }

    @Test
    public void testFindDir_NotFound() {
        assertThrows(FileNotFoundException.class, () -> {
            OS.findDir("nonexistent");
        });
    }

    @Test
    public void testFindFile() {
        File file = OS.findFile("Chronicle-Core", "src", "test", "resources", "sample.system.properties");
        assertNotNull(file);
        assertTrue(file.isFile());
        assertEquals("sample.system.properties", file.getName());

        File parentDir = file.getParentFile();
        assertNotNull(parentDir);
        assertTrue(parentDir.isDirectory());
        assertEquals("resources", parentDir.getName());

        File grandparentDir = parentDir.getParentFile();
        assertNotNull(grandparentDir);
        assertTrue(grandparentDir.isDirectory());
        assertEquals("test", grandparentDir.getName());
    }

    @Test
    public void testFindFile_NotFound() {
        File nonexistent = OS.findFile("nonexistent", "file.txt");
        File parentDir = nonexistent.getParentFile();
        assertNotNull(parentDir);
        assertTrue(parentDir.isDirectory());
        assertEquals(".", parentDir.getName());
    }

    @Test
    public void testGetHostName() {
        String hostName = OS.getHostName();
        assertNotNull(hostName);
        assertFalse(hostName.isEmpty());
    }

    @Test
    public void testGetIPAddress() {
        String ipAddress = OS.getIPAddress();
        assertNotNull(ipAddress);
        assertFalse(ipAddress.isEmpty());
    }

    @Test
    public void testGetUserName() {
        String userName = OS.getUserName();
        assertNotNull(userName);
        assertFalse(userName.isEmpty());
    }

    @Test
    public void testGetTarget() {
        String target = OS.getTarget();
        assertNotNull(target);
        assertFalse(target.isEmpty());
    }

    @Test
    public void testGetTmp() {
        String tmp = OS.getTmp();
        assertNotNull(tmp);
        assertFalse(tmp.isEmpty());
    }
}
