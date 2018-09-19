package net.openhft.chronicle.core.util;

import java.security.SecureRandom;

/*
 * Created by Peter Lawrey on 03/02/2017.
 */
public class ThreadLocalSecureRandom {
    static final ThreadLocal<SecureRandom> SECURE_RANDOM_TL = new ThreadLocal<>();

    public static SecureRandom current() {
        SecureRandom sr = SECURE_RANDOM_TL.get();
        if (sr == null)
            SECURE_RANDOM_TL.set(sr = new SecureRandom());
        return sr;
    }
}
