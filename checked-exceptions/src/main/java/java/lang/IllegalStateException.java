package java.lang;


public class IllegalStateException extends Exception {
    public IllegalStateException(String message) {
        super(message);
    }

    public IllegalStateException(Throwable cause) {
        super(cause);
    }
}
