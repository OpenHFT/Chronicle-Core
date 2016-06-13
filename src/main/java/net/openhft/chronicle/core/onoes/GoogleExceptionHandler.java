package net.openhft.chronicle.core.onoes;

/**
 * Created by Peter on 07/04/2016.
 */
public class GoogleExceptionHandler extends WebExceptionHandler {
    public static final ExceptionHandler WARN = new GoogleExceptionHandler(Slf4jExceptionHandler.WARN);

    public GoogleExceptionHandler(ExceptionHandler fallBack) {
        super("Google.properties", fallBack);
    }
}
