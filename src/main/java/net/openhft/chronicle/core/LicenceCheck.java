/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core;

import net.openhft.chronicle.core.io.IOTools;

import javax.naming.TimeLimitExceededException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.function.BiConsumer;

import static net.openhft.chronicle.core.Jvm.startup;
import static net.openhft.chronicle.core.Jvm.warn;

/**
 * The {@code LicenceCheck} interface provides mechanisms for checking the validity of a product's license.
 * It includes methods to verify license expiration and to log appropriate warnings or information based on the license status.
 */
public interface LicenceCheck {

    String CHRONICLE_LICENSE = "chronicle.license";

    /**
     * Checks for license expiry and logs a message with the license and expiry details.
     * If the license is nearing expiry, a warning is issued. Otherwise, startup information is logged.
     *
     * @param product the product name associated with the license
     * @param caller  the class initiating the license check
     */
    static void check(String product, Class<?> caller) {
        // Logs license expiry details based on the number of days left and the owner's information.
        final BiConsumer<Long, String> logLicenceExpiryDetails = (days, owner) -> {
            String ownerId = owner == null ? "" : "for " + owner + " "; // Determines the owner information if available.
            String expires = "The license " + ownerId + "expires";
            String message = days <= 1 ? expires + " in 1 day" : expires + " in " + days + " days"; // Constructs the expiry message.

            if (days > 500) // If more than 500 days remain, show the expiry in years.
                message = expires + " in about " + (days / 365) + " years";

            if (days < 30) // Logs a warning if less than 30 days remain.
                warn().on(LicenceCheck.class, message + ". At which point, this product will stop working, if you wish to renew this licence please contact sales@chronicle.software");
            else // Logs startup information for other cases.
                startup().on(LicenceCheck.class, message + ".");
        };

        licenceExpiry(product, caller, logLicenceExpiryDetails); // Calls the method to get the expiry details and logs them.
    }

    /**
     * Provides license expiry details by reading the expiry date from a file or system property.
     * It then invokes a callback with the number of days remaining until expiry and the license owner's information.
     *
     * @param product              the product name associated with the license
     * @param caller               the class initiating the license check
     * @param licenceExpiryDetails a callback that receives the number of days until license expiry and the owner's name
     */
    static void licenceExpiry(String product, Class<?> caller, BiConsumer<Long, String> licenceExpiryDetails) {
        String key = Jvm.getProperty(CHRONICLE_LICENSE); // Retrieves the license key from system properties.
        if (key == null || !key.contains(product + '.')) { // If the key is not found or doesn't match the product.
            String expiryDateFile = product + ".expiry-date"; // Determines the expiry date file for the product.
            try {
                String source = new String(IOTools.readFile(LicenceCheck.class, expiryDateFile)); // Reads the expiry date from the file.
                LocalDate expiryDate = LocalDate.parse(source.trim()); // Parses the expiry date.
                long days = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate); // Calculates days between now and expiry.
                if (days < 0)
                    throw Jvm.rethrow(new TimeLimitExceededException("Failed to read '" + expiryDateFile));
                licenceExpiryDetails.accept(days, null); // Passes the days remaining to the callback.
            } catch (Throwable t) { // Handles any errors during file reading or parsing.
                throw Jvm.rethrow(new TimeLimitExceededException("Failed to read expiry date, from '" + expiryDateFile + "'"));
            }
        } else {
            // Extracts expiry date and owner information from the license key.
            int start = key.indexOf("expires=") + 8;
            int end = key.indexOf(",", start);
            LocalDate date = LocalDate.parse(key.substring(start, end));
            int start2 = key.indexOf("owner=") + 6;
            int end2 = key.indexOf(",", start2);
            long days = date.toEpochDay() - System.currentTimeMillis() / 86400000;
            if (days < 0)
                throw Jvm.rethrow(new TimeLimitExceededException());
            String owner = key.substring(start2, end2); // Extracts the owner name from the key.
            licenceExpiryDetails.accept(days, owner); // Passes the days remaining and owner to the callback.
        }
    }

    /**
     * Checks if the function about to be called is part of an enterprise product.
     * If the license validation fails, a runtime exception will be thrown.
     */
    void licenceCheck();

    /**
     * Indicates whether the product is available, likely based on license validation.
     *
     * @return {@code true} if the product is available; {@code false} otherwise
     */
    boolean isAvailable();

}
