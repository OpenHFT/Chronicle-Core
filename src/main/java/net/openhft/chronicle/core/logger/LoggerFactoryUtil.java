package net.openhft.chronicle.core.logger;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public final class LoggerFactoryUtil {

    // Suppresses default constructor, ensuring non-instantiability.
    private LoggerFactoryUtil() {
    }

    @NotNull
    public static Logger initialize(@NotNull final Logger logger) {
        logger.isDebugEnabled();
        return logger;
    }

}