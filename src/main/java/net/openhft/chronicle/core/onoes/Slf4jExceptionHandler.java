package net.openhft.chronicle.core.onoes;

import org.slf4j.LoggerFactory;

/**
 * Created by Peter on 13/06/2016.
 */
public enum Slf4jExceptionHandler implements ExceptionHandler {
    FATAL {
        @Override
        public void on(Class clazz, String message, Throwable thrown) {
            LoggerFactory.getLogger(clazz).error("FATAL error " + message, thrown);
            System.exit(-1);
        }
    },
    WARN {
        @Override
        public void on(Class clazz, String message, Throwable thrown) {
            LoggerFactory.getLogger(clazz).warn(message, thrown);
        }
    },
    DEBUG {
        @Override
        public void on(Class clazz, String message, Throwable thrown) {
            LoggerFactory.getLogger(clazz).debug(message, thrown);
        }
    }
}
