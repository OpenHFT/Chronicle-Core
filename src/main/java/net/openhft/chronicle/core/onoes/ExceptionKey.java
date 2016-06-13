package net.openhft.chronicle.core.onoes;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Peter on 13/06/2016.
 */
public class ExceptionKey {
    public final LogLevel level;
    public final Class clazz;
    public final String message;
    public final Throwable throwable;

    public ExceptionKey(LogLevel level, Class clazz, String message, Throwable throwable) {
        this.level = level;
        this.clazz = clazz;
        this.message = message;
        this.throwable = throwable;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || toString().equals(obj.toString());
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        if (throwable != null)
        throwable.printStackTrace(new PrintWriter(sw));
        return "ExceptionKey{" +
                "level=" + level +
                ", clazz=" + clazz +
                ", message='" + message + '\'' +
                ", throwable=" + sw +
                '}';
    }
}
