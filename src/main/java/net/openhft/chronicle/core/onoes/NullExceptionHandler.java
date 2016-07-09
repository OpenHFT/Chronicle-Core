package net.openhft.chronicle.core.onoes;

/**
 * Created by Peter on 13/06/2016.
 */
public enum NullExceptionHandler implements ExceptionHandler {
    NOTHING {
        @Override
        public void on(Class clazz, String message, Throwable thrown) {

        }

        @Override
        public boolean isEnabled(Class aClass) {
            return false;
        }
    }
}
