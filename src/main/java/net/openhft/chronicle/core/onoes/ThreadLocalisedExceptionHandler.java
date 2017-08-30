package net.openhft.chronicle.core.onoes;

/*
 * Created by peter.lawrey@chronicle.software on 31/07/2017
 */
public class ThreadLocalisedExceptionHandler implements ExceptionHandler {
    private ExceptionHandler defaultHandler;
    private ThreadLocal<ExceptionHandler> handlerTL;

    public ThreadLocalisedExceptionHandler(Slf4jExceptionHandler handler) {
        defaultHandler = handler;
        resetThreadLocalHandler();
    }

    @Override
    public void on(Class clazz, String message, Throwable thrown) {
        ExceptionHandler exceptionHandler = handlerTL.get();
        if (exceptionHandler == null)
            exceptionHandler = defaultHandler;
        if (exceptionHandler == null)
            return;
        exceptionHandler.on(clazz, message, thrown);
    }

    public ExceptionHandler defaultHandler() {
        return defaultHandler;
    }

    public ThreadLocalisedExceptionHandler defaultHandler(ExceptionHandler defaultHandler) {
        this.defaultHandler = defaultHandler == null ? NullExceptionHandler.NOTHING : defaultHandler;
        return this;
    }

    public ExceptionHandler threadLocalHandler() {
        return handlerTL.get();
    }


    public ThreadLocalisedExceptionHandler threadLocalHandler(ExceptionHandler handler) {
        handlerTL.set(handler);
        return this;
    }

    public void resetThreadLocalHandler() {
        handlerTL = new InheritableThreadLocal<>();
    }
}
