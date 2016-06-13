package net.openhft.chronicle.core.onoes;

import java.util.Map;

/**
 * Created by Peter on 13/06/2016.
 */
public class RecordingExceptionHandler implements ExceptionHandler {
    private final LogLevel level;
    private final Map<ExceptionKey, Integer> exceptionKeyCountMap;

    public RecordingExceptionHandler(LogLevel level, Map<ExceptionKey, Integer> exceptionKeyCountMap) {
        this.level = level;
        this.exceptionKeyCountMap = exceptionKeyCountMap;
    }

    @Override
    public void on(Class clazz, String message, Throwable thrown) {
        synchronized (exceptionKeyCountMap) {
            ExceptionKey key = new ExceptionKey(level, clazz, message, thrown);
            exceptionKeyCountMap.merge(key, 1, (p, v) -> p == null ? v : p + v);
        }
    }
}
