package net.openhft.chronicle.core.onoes;

/**
 * Created by Peter on 07/04/2016.
 */
public class StackoverflowExceptionHandler extends WebExceptionHandler {
    public static final ExceptionHandler WARN = new StackoverflowExceptionHandler(Slf4jExceptionHandler.WARN);

    public StackoverflowExceptionHandler(ExceptionHandler fallBack) {
        super("Stackoverflow.properties", fallBack);
    }
}
