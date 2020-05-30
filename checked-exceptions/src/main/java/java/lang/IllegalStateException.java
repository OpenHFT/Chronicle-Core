package java.lang;

public class IllegalStateException extends Exception {
    public IllegalStateException(String message) {
        super(message);
    }

    public IllegalStateException(Throwable cause) {
        super(cause);
    }

    public IllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
