/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.internal.ChronicleGuarding;
import net.openhft.chronicle.core.io.IOTools;

import javax.naming.TimeLimitExceededException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.function.BiConsumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.openhft.chronicle.core.Jvm.startup;
import static net.openhft.chronicle.core.Jvm.warn;

public interface LicenceCheck {

    String CHRONICLE_LICENSE = "chronicle.license";

    /**
     * Check for license expiry and log message with license and expiry details
     *
     * @param product product
     * @param caller  caller
     */
    static void check(String product, Class<?> caller) {
        final BiConsumer<Long, String> logLicenceExpiryDetails = (days, owner) -> {
            String ownerId = owner == null ? "" : "for " + owner + " ";
            String expires = "The license " + ownerId + "expires";
            String message = days <= 1 ? expires + " in 1 day" : expires + " in " + days + " days";

            if (days > 500)
                message = expires + " in about " + (days / 365) + " years";

            if (days < 30)
                warn().on(LicenceCheck.class, message + ". At which point, this product will stop working, if you wish to renew this licence please contact sales@chronicle.software");
            else
                startup().on(LicenceCheck.class, message + ".");
        };

        licenceExpiry(product, caller, logLicenceExpiryDetails);
    }

    static boolean isJGuardProtected() {
        try {
            //noinspection JavaReflectionMemberAccess
            ChronicleGuarding.class.getDeclaredField("isDecrypted");
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    /**
     * Provide licence expiry details
     *
     * @param product              product
     * @param caller               caller
     * @param licenceExpiryDetails callback to call with license days to run and license owner
     */
    static void licenceExpiry(String product, Class<?> caller, BiConsumer<Long, String> licenceExpiryDetails) {
        if (isJGuardProtected())
            return;
        String key = Jvm.getProperty(CHRONICLE_LICENSE); // make sure this was loaded first.
        if (key == null || !key.contains(product + '.')) {
            String expiryDateFile = product + ".expiry-date";
            try {
                String source = new String(IOTools.readFile(LicenceCheck.class, expiryDateFile), UTF_8);
                LocalDate expiryDate = LocalDate.parse(source.trim());
                long days = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
                if (days < 0)
                    throw Jvm.rethrow(new TimeLimitExceededException("Failed to read '" + expiryDateFile));
                licenceExpiryDetails.accept(days, null);
            } catch (Throwable t) {
                throw Jvm.rethrow(new TimeLimitExceededException("Failed to read expiry date, from '" + expiryDateFile + "'"));
            }
        } else {
            int start = key.indexOf("expires=") + 8;
            int end = key.indexOf(",", start);
            LocalDate date = LocalDate.parse(key.substring(start, end));
            int start2 = key.indexOf("owner=") + 6;
            int end2 = key.indexOf(",", start2);
            long days = date.toEpochDay() - System.currentTimeMillis() / 86400000;
            if (days < 0)
                throw Jvm.rethrow(new TimeLimitExceededException());
            String owner = key.substring(start2, end2);
            licenceExpiryDetails.accept(days, owner);
        }
    }

    /**
     * checks if the function you are about to call is part of an enterprise product, if the licence
     * fails a runtime exception will be thrown
     */
    void licenceCheck();

    boolean isAvailable();

}
