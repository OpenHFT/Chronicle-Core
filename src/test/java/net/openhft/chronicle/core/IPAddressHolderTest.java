package net.openhft.chronicle.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class IPAddressHolderTest extends CoreTestCommon {

    @Test
    public void testIsSetVariousInputs() {
        // null should return false
        assertFalse(OS.IPAddressHolder.isSet(null));

        // empty string should return false
        assertFalse(OS.IPAddressHolder.isSet(""));

        // the NO_ADDRESS constant should return false
        assertFalse(OS.IPAddressHolder.isSet(OS.IPAddressHolder.NO_ADDRESS));

        // valid IP addresses should return true
        assertTrue(OS.IPAddressHolder.isSet("127.0.0.1"));
        assertTrue(OS.IPAddressHolder.isSet("192.168.0.1"));
    }
}
